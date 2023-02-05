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

package org.limbo.flowjob.broker.application.plan.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInstance;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.WorkflowPlan;
import org.limbo.flowjob.broker.dao.entity.JobInfoEntity;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.dag.DAGNode;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/1/4
 */
@Slf4j
@Component
public class WorkflowScheduleStrategy extends AbstractScheduleStrategy {

    @Override
    protected void schedulePlan(TriggerType triggerType, Plan plan, LocalDateTime triggerAt) {
        String planId = plan.getPlanId();
        String version = plan.getVersion();
        WorkflowPlan workflowPlan = (WorkflowPlan) plan;

        DAG<WorkflowJobInfo> dag = workflowPlan.getDag();
        List<String> jobIds = dag.nodes().stream().map(DAGNode::getId).collect(Collectors.toList());
        List<JobInfoEntity> jobInfoEntities = jobInfoEntityRepo.findAllById(jobIds);
        Verifies.notEmpty(jobInfoEntities, "does not find " + planId + " plan's job info by version--" + version + "");

        // 保存 planInstance
        String planInstanceId = savePlanInstanceEntity(planId, version, triggerType, triggerAt);

        // 获取头部节点
        List<JobInstance> rootJobs = new ArrayList<>();

        for (WorkflowJobInfo jobInfo : workflowPlan.getDag().origins()) {
            if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                rootJobs.add(newJobInstance(planId, version, plan.planType(), planInstanceId, jobInfo, triggerAt));
            }
        }

        // 如果root都为api触发则为空 交由api创建
        if (CollectionUtils.isNotEmpty(rootJobs)) {
            scheduleJobInstances(rootJobs, triggerAt);
        }
    }

    public WorkflowPlan toPlanInfo(PlanInfoEntity entity, List<JobInfoEntity> jobInfoEntities) {
        return new WorkflowPlan(
                entity.getPlanId(),
                entity.getPlanInfoId(),
                TriggerType.parse(entity.getTriggerType()),
                domainConverter.toScheduleOption(entity),
                domainConverter.toJobDag(entity.getJobInfo(), jobInfoEntities)
        );
    }

    @Override
    public void handleJobSuccess(JobInstance jobInstance) {
        jobInstanceEntityRepo.updateStatusSuccess(jobInstance.getJobInstanceId());

        String planId = jobInstance.getPlanId();
        String version = jobInstance.getPlanVersion();
        String planInstanceId = jobInstance.getPlanInstanceId();
        JobInfo jobInfo = jobInstance.getJobInfo();
        String jobId = jobInfo.getId();

        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(version).orElse(null);

        DAG<WorkflowJobInfo> dag = domainConverter.toJobDag(planInfoEntity.getJobInfo(), null);
        List<String> jobIds = dag.nodes().stream().map(DAGNode::getId).collect(Collectors.toList());
        List<JobInfoEntity> jobInfoEntities = jobInfoEntityRepo.findAllById(jobIds);
        dag = domainConverter.toJobDag(planInfoEntity.getJobInfo(), jobInfoEntities);

        // 当前节点的子节点
        List<WorkflowJobInfo> subJobInfos = dag.subNodes(jobId);

        if (CollectionUtils.isEmpty(subJobInfos)) {
            // 当前节点为叶子节点 检测 Plan 实例是否已经执行完成
            // 1. 所有节点都已经成功或者失败 2. 这里只关心plan的成功更新，失败是在task回调
            if (checkJobsSuccessOrIgnoreError(planInstanceId, dag.lasts())) {
                planInstanceEntityRepo.success(planInstanceId, TimeUtils.currentLocalDateTime());
            }
        } else {
            LocalDateTime triggerAt = TimeUtils.currentLocalDateTime();
            // 后续作业存在，则检测是否可触发，并继续下发作业
            List<JobInstance> subJobInstances = new ArrayList<>();
            for (WorkflowJobInfo subJobInfo : subJobInfos) {
                // 前置节点已经完成则可以下发
                if (checkJobsSuccessOrIgnoreError(planInstanceId, dag.preNodes(subJobInfo.getId()))) {
                    subJobInstances.add(newJobInstance(planId, version, PlanType.parse(planInfoEntity.getPlanType()), planInstanceId, subJobInfo, triggerAt));
                }
            }

            if (CollectionUtils.isNotEmpty(subJobInstances)) {
                scheduleJobInstances(subJobInstances, triggerAt);
            }

        }
    }

    @Override
    public void handleJobFail(JobInstance jobInstance) {
        WorkflowJobInstance workflowJobInstance = (WorkflowJobInstance) jobInstance;
        WorkflowJobInfo workflowJobInfo = workflowJobInstance.getWorkflowJobInfo();
        if (workflowJobInfo.isTerminateWithFail()) {
            super.handleJobFail(jobInstance);
        } else {
            handleJobSuccess(jobInstance);
        }
    }

    /**
     * 校验 planInstance 下对应 job 的 jobInstance 是否都执行成功 或者失败了但是可以忽略失败
     */
    private boolean checkJobsSuccessOrIgnoreError(String planInstanceId, List<WorkflowJobInfo> jobInfos) {
        if (CollectionUtils.isEmpty(jobInfos)) {
            return true;
        }
        Map<String, WorkflowJobInfo> jobInfoMap = jobInfos.stream().collect(Collectors.toMap(DAGNode::getId, jobInfo -> jobInfo));
        List<JobInstanceEntity> entities = jobInstanceEntityRepo.findByPlanInstanceIdAndJobIdIn(planInstanceId, new LinkedList<>(jobInfoMap.keySet()));
        if (CollectionUtils.isEmpty(entities) || jobInfos.size() > entities.size()) {
            // 按新流程 job 应该统一创建 不存在有些job还未创建情况的
            log.warn("job doesn't create completable in PlanInstance:{} where jobIds:{}", planInstanceId, jobInfoMap.keySet());
            return false;
        }
        for (JobInstanceEntity entity : entities) {
            if (entity.getStatus() == JobStatus.SUCCEED.status) {
                // 成功的
            } else if (entity.getStatus() == JobStatus.FAILED.status) {
                // 失败的 看是否忽略失败
                WorkflowJobInfo jobInfo = jobInfoMap.get(entity.getJobId());
                if (jobInfo.isTerminateWithFail()) {
                    return false;
                }
            } else {
                // 执行中
                return false;
            }
        }

        return true;
    }

    public JobInstance newJobInstance(String planId, String planVersion, PlanType planType, String planInstanceId, WorkflowJobInfo workflowJobInfo, LocalDateTime triggerAt) {
        WorkflowJobInstance instance = new WorkflowJobInstance();
        instance.setJobInstanceId(idGenerator.generateId(IDType.JOB_INSTANCE));
        instance.setPlanId(planId);
        instance.setPlanInstanceId(planInstanceId);
        instance.setPlanVersion(planVersion);
        instance.setPlanType(planType);
        instance.setWorkflowJobInfo(workflowJobInfo);
        instance.setStatus(JobStatus.SCHEDULING);
        instance.setTriggerAt(triggerAt);
        return instance;
    }

}
