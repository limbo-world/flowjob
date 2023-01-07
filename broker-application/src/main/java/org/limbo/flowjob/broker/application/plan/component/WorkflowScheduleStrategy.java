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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInfo;
import org.limbo.flowjob.broker.core.domain.plan.WorkflowPlanInfo;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.exceptions.JobException;
import org.limbo.flowjob.broker.dao.entity.JobInfoEntity;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.TaskType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.dag.DAGNode;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Override
    protected void schedulePlanInfo(PlanInfo planInfo, LocalDateTime triggerAt) {
        String planId = planInfo.getPlanId();
        String version = planInfo.getVersion();
        List<JobInfoEntity> jobInfoEntities = jobInfoEntityRepo.findByPlanInfoId(version);
        Verifies.notEmpty(jobInfoEntities, "does not find " + planId + " plan's job info by version--" + version + "");

        // 保存 planInstance todo 触发类型 手动触发？？
        String planInstanceId = savePlanInstanceEntity(planId, version, TriggerType.SCHEDULE, triggerAt);

        WorkflowPlanInfo workflowPlanInfo = (WorkflowPlanInfo) planInfo;

        // 获取头部节点
        List<JobInstance> rootJobs = new ArrayList<>();

        for (JobInfo jobInfo : workflowPlanInfo.getDag().origins()) {
            if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                rootJobs.add(newJobInstance(planId, version, planInfo.planType(), planInstanceId, jobInfo, TimeUtils.currentLocalDateTime()));
            }
        }

        // 如果root都为api触发则为空 交由api创建
        if (CollectionUtils.isNotEmpty(rootJobs)) {
            scheduleJobInstances(rootJobs);
        }
    }

    public WorkflowPlanInfo toPlanInfo(PlanInfoEntity entity, List<JobInfoEntity> jobInfoEntities) {
        return new WorkflowPlanInfo(
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
        String jobId = jobInstance.getJobId();

        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(version).orElse(null);
        List<JobInfoEntity> jobInfoEntities = jobInfoEntityRepo.findByPlanInfoId(planInfoEntity.getPlanInfoId());
        DAG<JobInfo> dag = domainConverter.toJobDag(planInfoEntity.getJobInfo(), jobInfoEntities);

        // 当前节点的子节点
        List<JobInfo> subJobInfos = dag.subNodes(jobId);

        if (CollectionUtils.isEmpty(subJobInfos)) {
            // 当前节点为叶子节点 检测 Plan 实例是否已经执行完成
            // 1. 所有节点都已经成功或者失败 2. 这里只关心plan的成功更新，失败是在task回调
            if (checkJobsSuccessOrIgnoreError(planInstanceId, dag.lasts())) {
                planInstanceEntityRepo.success(planInstanceId, TimeUtils.currentLocalDateTime());
            }
        } else {

            // 后续作业存在，则检测是否可触发，并继续下发作业
            List<JobInstance> subJobInstances = new ArrayList<>();
            for (JobInfo subJobInfo : subJobInfos) {
                // 前置节点已经完成则可以下发
                if (checkJobsSuccessOrIgnoreError(planInstanceId, dag.preNodes(subJobInfo.getId()))) {
                    subJobInstances.add(newJobInstance(planId, version, PlanType.parse(planInfoEntity.getPlanType()), planInstanceId, subJobInfo, TimeUtils.currentLocalDateTime()));
                }
            }

            if (CollectionUtils.isNotEmpty(subJobInstances)) {
                scheduleJobInstances(subJobInstances);
            }

        }
    }

    @Override
    public void handleJobFail(JobInstance jobInstance) {
        if (jobInstance.isTerminateWithFail()) {

            jobInstanceEntityRepo.updateStatusExecuteFail(jobInstance.getJobInstanceId(), MsgConstants.TASK_FAIL);

            if (jobInstance.retry()) {
                scheduleJobInstances(Collections.singletonList(jobInstance));
            } else {
                planInstanceEntityRepo.fail(jobInstance.getPlanInstanceId(), TimeUtils.currentLocalDateTime());
            }
        } else {
            handleJobSuccess(jobInstance);
        }
    }

    /**
     * 调度jobInstance
     */
    private void scheduleJobInstances(List<JobInstance> jobInstances) {
        if (CollectionUtils.isEmpty(jobInstances)) {
            return;
        }

        // 保存 jobInstance
        List<JobInstanceEntity> jobInstanceEntities = jobInstances.stream().map(domainConverter::toJobInstanceEntity).collect(Collectors.toList());
        jobInstanceEntityRepo.saveAll(jobInstanceEntities);
        jobInstanceEntityRepo.flush();

        List<Task> tasks = new ArrayList<>();
        for (JobInstance instance : jobInstances) {
            // 根据job类型创建task
            List<Task> jobTasks;
            switch (instance.getType()) {
                case NORMAL:
                    jobTasks = taskFactory.create(instance, instance.getTriggerAt(), TaskType.NORMAL);
                    break;
                case BROADCAST:
                    jobTasks = taskFactory.create(instance, instance.getTriggerAt(), TaskType.BROADCAST);
                    break;
                case MAP:
                case MAP_REDUCE:
                    jobTasks = taskFactory.create(instance, instance.getTriggerAt(), TaskType.SPLIT);
                    break;
                default:
                    throw new JobException(instance.getJobId(), MsgConstants.UNKNOWN + " job type:" + instance.getType().type);
            }

            // 如果可以创建的任务为空（只有广播任务）
            if (CollectionUtils.isEmpty(jobTasks)) {
                handleJobSuccess(instance);
            } else {
                tasks.addAll(jobTasks);
            }
        }

        saveAndScheduleTask(tasks);
    }

    /**
     * 校验 planInstance 下对应 job 的 jobInstance 是否都执行成功 或者失败了但是可以忽略失败
     */
    private boolean checkJobsSuccessOrIgnoreError(String planInstanceId, List<JobInfo> jobInfos) {
        if (CollectionUtils.isEmpty(jobInfos)) {
            return true;
        }
        Map<String, JobInfo> jobInfoMap = jobInfos.stream().collect(Collectors.toMap(DAGNode::getId, jobInfo -> jobInfo));
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
                JobInfo jobInfo = jobInfoMap.get(entity.getJobId());
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

}
