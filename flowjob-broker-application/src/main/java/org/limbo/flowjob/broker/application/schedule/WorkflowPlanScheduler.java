/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.application.schedule;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.WorkflowPlan;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Devil
 * @since 2023/8/7
 */
@Slf4j
@Component
public class WorkflowPlanScheduler extends AbstractPlanScheduler {

    @Override
    @Transactional
    public void scheduleJob(Plan plan, String planInstanceId, String jobId) {
        WorkflowPlan workflowPlan = (WorkflowPlan) plan;
        DAG<WorkflowJobInfo> dag = workflowPlan.getDag();
        WorkflowJobInfo jobInfo = dag.getNode(jobId);

        Verifies.verify(checkJobsSuccess(planInstanceId, dag.preNodes(jobInfo.getId()), true), "previous job is not complete, please wait!");

        Verifies.verify(TriggerType.API == jobInfo.getTriggerType(), "only api triggerType job can schedule by api");

        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(planInstanceId).orElse(null);

        List<JobInstance> jobInstances = createJobInstances(plan, new Attributes(planInstanceEntity.getAttributes()), planInstanceId, TimeUtils.currentLocalDateTime());

        saveAndScheduleJobInstances(jobInstances);
    }

    // todo 执行的时候可以选择 是就重新计算当前的还是后续节点是否也重新执行一遍
    @Override
    @Transactional
    public void manualScheduleJob(Plan plan, String planInstanceId, String jobId) {
        WorkflowPlan workflowPlan = (WorkflowPlan) plan;
        DAG<WorkflowJobInfo> dag = workflowPlan.getDag();
        WorkflowJobInfo jobInfo = dag.getNode(jobId);

        Verifies.verify(checkJobsSuccess(planInstanceId, dag.preNodes(jobInfo.getId()), true), "previous job is not complete, please wait!");

        JobInstance jobInstance = jobInstanceRepository.getLatest(planInstanceId, jobId);// 获取最后一条
        String newJobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
        jobInstance.retryReset(newJobInstanceId, 0);
        List<JobInstance> jobInstances = Collections.singletonList(jobInstance);

        saveAndScheduleJobInstances(jobInstances);
    }

    @Override
    public List<JobInstance> createJobInstances(Plan plan, Attributes planAttributes, String planInstanceId, LocalDateTime triggerAt) {
        List<JobInstance> jobInstances = new ArrayList<>();
        // 获取头部节点
        for (WorkflowJobInfo jobInfo : ((WorkflowPlan) plan).getDag().origins()) {
            if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                jobInstances.add(createJobInstance(plan.getPlanId(), plan.getVersion(), planInstanceId, planAttributes, new Attributes(), jobInfo, triggerAt));
            }
        }
        return jobInstances;
    }

    @Override
    public void handleJobSuccess(JobInstance jobInstance) {
        int num = jobInstanceEntityRepo.success(jobInstance.getJobInstanceId(), TimeUtils.currentLocalDateTime(), jobInstance.getContext().toString());
        if (num < 1) {
            return; // 被其他更新
        }

        String planInstanceId = jobInstance.getPlanInstanceId();

        String planId = jobInstance.getPlanId();
        String version = jobInstance.getPlanVersion();
        WorkflowJobInfo jobInfo = (WorkflowJobInfo) jobInstance.getJobInfo();
        String jobId = jobInfo.getId();

        DAG<WorkflowJobInfo> dag = DomainConverter.toJobDag(jobId);

        // 当前节点的子节点
        List<WorkflowJobInfo> subJobInfos = dag.subNodes(jobId);

        if (CollectionUtils.isEmpty(subJobInfos)) {
            // 当前节点为叶子节点 检测 Plan 实例是否已经执行完成
            // 1. 所有节点都已经成功或者失败 2. 这里只关心plan的成功更新，失败是在task回调
            if (checkJobsSuccess(planInstanceId, dag.lasts(), true)) {
                handlerPlanComplete(planInstanceId, true);
            }
        } else {
            PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(planInstanceId).orElse(null);

            LocalDateTime triggerAt = TimeUtils.currentLocalDateTime();
            // 后续作业存在，则检测是否可触发，并继续下发作业
            List<JobInstance> subJobInstances = new ArrayList<>();
            for (WorkflowJobInfo subJobInfo : subJobInfos) {
                // 前置节点已经完成则可以下发
                if (checkJobsSuccess(planInstanceId, dag.preNodes(subJobInfo.getId()), true)) {
                    JobInstance subJobInstance = createJobInstance(planId, version, planInstanceId, new Attributes(planInstanceEntity.getAttributes()), jobInstance.getContext(), subJobInfo, triggerAt);
                    subJobInstances.add(subJobInstance);
                }
            }

            if (CollectionUtils.isNotEmpty(subJobInstances)) {
                saveAndScheduleJobInstances(subJobInstances);
            }

        }
    }

    @Override
    public void handleJobFail(JobInstance jobInstance, String errorMsg) {
        int num = jobInstanceEntityRepo.fail(jobInstance.getJobInstanceId(), errorMsg);
        if (num < 1) {
            return; // 可能被其他的task处理了
        }

        WorkflowJobInfo jobInfo = (WorkflowJobInfo) jobInstance.getJobInfo();
        String planInstanceId = jobInstance.getPlanInstanceId();
        // 是否需要重试
        if (jobInstance.canRetry()) {
            String newJobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
            jobInstance.retryReset(newJobInstanceId, jobInfo.getRetryOption().getRetryInterval());
            saveAndScheduleJobInstances(Collections.singletonList(jobInstance));
        } else if (jobInfo.isContinueWhenFail()) {
            // 如果 配置job失败了也继续执行
            handleJobSuccess(jobInstance);
        } else {
            handlerPlanComplete(planInstanceId, false);
        }
    }

    @Override
    public PlanType getPlanType() {
        return PlanType.WORKFLOW;
    }

    /**
     * 校验 planInstance 下对应 job 的 jobInstance 是否都执行成功 或者失败了但是可以忽略失败
     * @param checkContinueWhenFail 和 continueWithFail 同时 true，当job执行失败，会认为执行成功
     */
    public boolean checkJobsSuccess(String planInstanceId, List<WorkflowJobInfo> jobInfos, boolean checkContinueWhenFail) {
        if (CollectionUtils.isEmpty(jobInfos)) {
            return true;
        }

        for (WorkflowJobInfo jobInfo : jobInfos) {
            JobInstanceEntity entity = jobInstanceEntityRepo.findByLatest(planInstanceId, jobInfo.getId());
            if (entity == null) {
                // 按新流程 job 应该统一创建 不存在有些job还未创建情况的
                log.warn("job doesn't create completable in PlanInstance:{} where jobId:{}", planInstanceId, jobInfo.getId());
                return false;
            }
            if (entity.getStatus() == JobStatus.FAILED.status) {
                if (!checkContinueWhenFail || !jobInfo.isContinueWhenFail()) {
                    return false;
                }

            } else if (entity.getStatus() != JobStatus.SUCCEED.status) {
                return false; // 执行中
            }
        }
        return true;
    }
}
