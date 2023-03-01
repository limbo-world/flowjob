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

package org.limbo.flowjob.broker.application.component.schedule;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.limbo.flowjob.broker.application.component.SlotManager;
import org.limbo.flowjob.broker.core.dispatch.TaskDispatcher;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.domain.task.TaskFactory;
import org.limbo.flowjob.broker.core.exceptions.JobException;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.PlanStatus;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.constants.TaskType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.exception.VerifyException;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.dag.DAGNode;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/1/4
 */
@Slf4j
@Component
public class ScheduleStrategyHelper {

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    @Setter(onMethod_ = @Inject)
    private TaskFactory taskFactory;

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private DomainConverter domainConverter;

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private TaskDispatcher taskDispatcher;

    @Setter(onMethod_ = @Inject)
    private SlotManager slotManager;

    @Setter(onMethod_ = @Inject)
    private PlanSlotEntityRepo planSlotEntityRepo;

    @Setter(onMethod_ = @Inject)
    private JobInstanceHelper jobInstanceHelper;

    @Transactional
    public PlanInstanceEntity lockAndSavePlanInstance(Plan plan, TriggerType triggerType, LocalDateTime triggerAt) {
        String planId = plan.getPlanId();
        String version = plan.getVersion();

        PlanEntity planEntity = planEntityRepo.selectForUpdate(planId);
        Verifies.notNull(planEntity, MsgConstants.CANT_FIND_PLAN + planId);
        Verifies.verify(!planEntity.isDeleted(), "plan:" + planId + " is deleted!");
        Verifies.verify(planEntity.isEnabled(), "plan:" + planId + " is not enabled!");
        // 任务是由之前时间创建的 调度时候如果版本改变 可能会有调度时间的变化本次就无需执行
        // 比如 5s 执行一次 分别在 5s 10s 15s 在11s的时候内存里下次执行为 15s 此时修改为 2s 执行一次 那么重新加载plan后应该为 12s 14s 所以15s这次可以跳过
        Verifies.verify(Objects.equals(version, planEntity.getCurrentVersion()), MessageFormat.format("plan:{0} version {1} change to {2}", planId, version, planEntity.getCurrentVersion()));

        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(version).orElseThrow(VerifyException.supplier(MessageFormat.format("does not find {0} plan info by version {1}", planId, version)));

        // 判断是否由当前节点执行
        if (TriggerType.API != triggerType) {

            Verifies.verify(planEntity.isEnabled(), "plan " + planId + " is not enabled");

            List<Integer> slots = slotManager.slots();
            Verifies.notEmpty(slots, "slots is empty");
            PlanSlotEntity planSlotEntity = planSlotEntityRepo.findByPlanId(planId);
            Verifies.notNull(planSlotEntity, "plan's slot is null id:" + planId);
            Verifies.verify(slots.contains(planSlotEntity.getSlot()), MessageFormat.format("plan {0} is not in this broker", planId));

            // 校验是否重复创建
            PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findLatelyTrigger(planId, planInfoEntity.getPlanInfoId(), planInfoEntity.getScheduleType(), triggerType.type);
            ScheduleType scheduleType = ScheduleType.parse(planInfoEntity.getScheduleType());
            switch (scheduleType) {
                case FIXED_RATE:
                case CRON:
                    Verifies.verify(planInstanceEntity == null || !triggerAt.isEqual(planInstanceEntity.getTriggerAt()),
                            MessageFormat.format("Duplicate create PlanInstance,triggerAt:{0} planId[{1}] Version[{2}] oldPlanInstance[{3}]",
                                    triggerAt, planId, version, JacksonUtils.toJSONString(planInstanceEntity))
                    );
                    break;
                case FIXED_DELAY:
                    Verifies.verify(planInstanceEntity == null || (!triggerAt.isEqual(planInstanceEntity.getTriggerAt()) && PlanStatus.parse(planInstanceEntity.getStatus()).isCompleted()),
                            MessageFormat.format("Please wait last PlanInstance[{0}] complete.Plan[{1}] Version[{2}]",
                                    JacksonUtils.toJSONString(planInstanceEntity), planId, version)
                    );
                    break;
                default:
                    throw new VerifyException(MsgConstants.UNKNOWN + " scheduleType " + planInfoEntity.getScheduleType());
            }
        }


        String planInstanceId = idGenerator.generateId(IDType.PLAN_INSTANCE);
        PlanInstanceEntity planInstanceEntity = new PlanInstanceEntity();
        planInstanceEntity.setPlanInstanceId(planInstanceId);
        planInstanceEntity.setPlanId(planId);
        planInstanceEntity.setPlanInfoId(version);
        planInstanceEntity.setStatus(PlanStatus.SCHEDULING.status);
        planInstanceEntity.setTriggerType(triggerType.type);
        planInstanceEntity.setScheduleType(planInfoEntity.getScheduleType());
        planInstanceEntity.setTriggerAt(triggerAt);
        planInstanceEntityRepo.saveAndFlush(planInstanceEntity);
        return planInstanceEntity;
    }

    @Transactional
    public JobInstance lockAndSaveWorkflowJobInstance(Plan plan, String planInstanceId, WorkflowJobInfo jobInfo, LocalDateTime triggerAt) {
        planInstanceEntityRepo.selectForUpdate(planInstanceId);

        long count = jobInstanceEntityRepo.countByPlanInstanceIdAndJobId(planInstanceId, jobInfo.getJob().getId());
        if (count > 0) {
            return null;
        }

        JobInstance jobInstance = jobInstanceHelper.newWorkflowJobInstance(plan.getPlanId(), plan.getVersion(), planInstanceId, new Attributes(), jobInfo, triggerAt);
        jobInstanceEntityRepo.saveAndFlush(DomainConverter.toJobInstanceEntity(jobInstance));
        return jobInstance;
    }

    @Transactional
    public void schedule(Task task) {
        if (task.getStatus() != TaskStatus.SCHEDULING) {
            return;
        }

        // 根据job id 分组 取最新的 判断是否失败 如果已经有 job 失败且终止的，则直接返回失败
        List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findByPlanInstanceId(task.getPlanInstanceId());
        Map<String, List<JobInstanceEntity>> jobInstanceMap = jobInstanceEntities.stream().collect(Collectors.groupingBy(JobInstanceEntity::getPlanInstanceId));
        for (Map.Entry<String, List<JobInstanceEntity>> entry : jobInstanceMap.entrySet()) {
            List<JobInstanceEntity> entities = entry.getValue();
            if (CollectionUtils.isEmpty(entities)) {
                continue;
            }
            entities = entities.stream().sorted((o1, o2) -> (int) (o2.getId() - o1.getId())).collect(Collectors.toList());
            JobInstanceEntity latest = entities.get(0);
            if (JobStatus.FAILED.getStatus() == latest.getStatus() && BooleanUtils.isTrue(latest.getTerminateWithFail())) {
                handleFail(task, MsgConstants.TERMINATE_BY_OTHER_JOB, null);
                break;
            }
        }

        int num = taskEntityRepo.dispatching(task.getTaskId());
        if (num < 1) {
            return; // 可能多个节点操作同个task
        }
        task.setStatus(TaskStatus.DISPATCHING);

        // 下面两个可能会被其他task更新 但是这是正常的
        jobInstanceEntityRepo.executing(task.getJobInstanceId(), TimeUtils.currentLocalDateTime());
        planInstanceEntityRepo.executing(task.getPlanInstanceId(), TimeUtils.currentLocalDateTime());

        boolean dispatched = taskDispatcher.dispatch(task);
        if (dispatched) {
            // 下发成功
            taskEntityRepo.executing(task.getTaskId(), task.getWorkerId(), TimeUtils.currentLocalDateTime());
        } else {
            // 下发失败
            handleFail(task, MsgConstants.DISPATCH_FAIL, null);
        }
    }

    @Transactional
    public void handleSuccess(Task task, Object result) {
        int num = taskEntityRepo.success(task.getTaskId(), TimeUtils.currentLocalDateTime(),
                task.getContext().toString(), task.getJobAttributes().toString(), JacksonUtils.toJSONString(result)
        );

        if (num != 1) { // 已经被更新 无需重复处理
            return;
        }

        JobInstance jobInstance = jobInstanceHelper.getJobInstance(task.getJobInstanceId());
        if (JobStatus.EXECUTING != jobInstance.getStatus()) {
            log.warn("task:{} update status success but jobInstance:{} is already changed", task.getTaskId(), task.getJobInstanceId());
            return;
        }
        // 检查task是否都已经完成
        List<TaskEntity> taskEntities = taskEntityRepo.findByJobInstanceIdAndType(task.getJobInstanceId(), task.getType().type);
        boolean success = taskEntities.stream().allMatch(entity -> TaskStatus.SUCCEED == TaskStatus.parse(entity.getStatus()));
        if (!success) {
            return; // 交由失败的task 或者后面还在执行的task去做后续逻辑处理
        }
        // 聚合上下文内容
        Attributes context = new Attributes();
        for (TaskEntity taskEntity : taskEntities) {
            Attributes taskContext = new Attributes(taskEntity.getContext());
            context.put(taskContext);
        }
        jobInstance.setContext(context);
        // 聚合job参数
        Attributes jobAttributes = jobInstance.getJobAttributes();
        for (TaskEntity taskEntity : taskEntities) {
            Attributes taskJobAttrs = new Attributes(taskEntity.getJobAttributes());
            jobAttributes.put(taskJobAttrs);
        }
        // 判断当前 job 类型 进行后续处理
        JobInfo jobInfo = jobInstance.getJobInfo();
        switch (jobInfo.getType()) {
            case NORMAL:
            case BROADCAST:
                handleJobSuccess(jobInstance);
                break;
            case MAP:
                handleMapJobSuccess(task, jobInstance);
                break;
            case MAP_REDUCE:
                handleMapReduceJobSuccess(task, jobInstance);
                break;
            default:
                throw new IllegalArgumentException(MsgConstants.UNKNOWN + " JobType in jobInstance:" + jobInstance.getJobInstanceId());
        }
    }

    @Transactional
    public void handleFail(Task task, String errorMsg, String errorStackTrace) {
        int num = taskEntityRepo.fail(task.getTaskId(), task.getStatus().status, TimeUtils.currentLocalDateTime(), errorMsg, errorStackTrace);

        if (num < 1) {
            return; // 并发更新过了 正常来说前面job更新成功 这个不可能会进来
        }

        JobInstance jobInstance = jobInstanceHelper.getJobInstance(task.getJobInstanceId());
        if (!jobInstance.isTerminateWithFail()) {
            handleJobSuccess(jobInstance);
            return;
        }

        num = jobInstanceEntityRepo.fail(task.getJobInstanceId(), MsgConstants.TASK_FAIL);
        if (num < 1) {
            return; // 可能被其他的task处理了
        }

        JobInfo jobInfo = jobInstance.getJobInfo();
        String planInstanceId = jobInstance.getPlanInstanceId();
        // 是否需要重试
        long retry = jobInstanceEntityRepo.countByPlanInstanceIdAndJobId(planInstanceId, jobInfo.getId());
        if (jobInfo.getRetryOption().getRetry() > retry) {
            jobInstanceHelper.retryReset(jobInstance, jobInfo.getRetryOption().getRetryInterval());
            saveAndScheduleJobInstances(Collections.singletonList(jobInstance), TimeUtils.currentLocalDateTime());
        } else {
            handlerPlanComplete(planInstanceId, false);
        }
    }

    private void handleMapJobSuccess(Task task, JobInstance jobInstance) {
        switch (task.getType()) {
            case SPLIT:
                handleSplitTaskSuccess(jobInstance);
                break;
            case MAP:
                handleJobSuccess(jobInstance);
                break;
            default:
                throw new IllegalArgumentException("Illegal TaskType in task:" + task.getTaskId());
        }
    }

    private void handleMapReduceJobSuccess(Task task, JobInstance jobInstance) {
        switch (task.getType()) {
            case SPLIT:
                handleSplitTaskSuccess(jobInstance);
                break;
            case MAP:
                saveTasks(taskFactory.create(jobInstance, TaskType.REDUCE), TimeUtils.currentLocalDateTime());
                break;
            case REDUCE:
                handleJobSuccess(jobInstance);
                break;
            default:
                throw new IllegalArgumentException("Illegal TaskType in task:" + task.getTaskId());
        }
    }

    private void handleSplitTaskSuccess(JobInstance jobInstance) {
        List<Task> tasks = taskFactory.create(jobInstance, TaskType.MAP);
        if (CollectionUtils.isEmpty(tasks)) {
            handleJobSuccess(jobInstance);
        } else {
            saveTasks(tasks, TimeUtils.currentLocalDateTime());
        }
    }

    private void handleJobSuccess(JobInstance jobInstance) {
        int num = jobInstanceEntityRepo.success(jobInstance.getJobInstanceId(), TimeUtils.currentLocalDateTime(), jobInstance.getContext().toString());
        if (num < 1) {
            return; // 被其他更新
        }

        String planInstanceId = jobInstance.getPlanInstanceId();

        if (PlanType.SINGLE == jobInstance.getPlanType()) {
            handlerPlanComplete(planInstanceId, true);
        } else {
            String planId = jobInstance.getPlanId();
            String version = jobInstance.getPlanVersion();
            JobInfo jobInfo = jobInstance.getJobInfo();
            String jobId = jobInfo.getId();

            PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(version).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INFO + version));

            DAG<WorkflowJobInfo> dag = DomainConverter.toJobDag(planInfoEntity.getJobInfo());
            // 当前节点的子节点
            List<WorkflowJobInfo> subJobInfos = dag.subNodes(jobId);

            if (CollectionUtils.isEmpty(subJobInfos)) {
                // 当前节点为叶子节点 检测 Plan 实例是否已经执行完成
                // 1. 所有节点都已经成功或者失败 2. 这里只关心plan的成功更新，失败是在task回调
                if (checkJobsSuccessOrIgnoreError(planInstanceId, dag.lasts())) {
                    handlerPlanComplete(planInstanceId, true);
                }
            } else {
                LocalDateTime triggerAt = TimeUtils.currentLocalDateTime();
                // 后续作业存在，则检测是否可触发，并继续下发作业
                List<JobInstance> subJobInstances = new ArrayList<>();
                for (WorkflowJobInfo subJobInfo : subJobInfos) {
                    // 前置节点已经完成则可以下发
                    if (checkJobsSuccessOrIgnoreError(planInstanceId, dag.preNodes(subJobInfo.getId()))) {
                        JobInstance subJobInstance = jobInstanceHelper.newWorkflowJobInstance(planId, version, planInstanceId, jobInstance.getContext(), subJobInfo, triggerAt);
                        subJobInstances.add(subJobInstance);
                    }
                }

                if (CollectionUtils.isNotEmpty(subJobInstances)) {
                    saveAndScheduleJobInstances(subJobInstances, triggerAt);
                }

            }
        }
    }

    public void handlerPlanComplete(String planInstanceId, boolean success) {
        if (success) {
            planInstanceEntityRepo.success(planInstanceId, TimeUtils.currentLocalDateTime());
        } else {
            planInstanceEntityRepo.fail(planInstanceId, TimeUtils.currentLocalDateTime());
        }
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(planInstanceId).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INSTANCE + planInstanceId));
        if (ScheduleType.FIXED_DELAY == ScheduleType.parse(planInstanceEntity.getScheduleType())) {
            // 如果为 FIXED_DELAY 更新 plan  使得 UpdatedPlanLoadTask 进行重新加载
            planEntityRepo.updateTime(planInstanceEntity.getPlanId(), TimeUtils.currentLocalDateTime());
        }
    }

    /**
     * 校验 planInstance 下对应 job 的 jobInstance 是否都执行成功 或者失败了但是可以忽略失败
     */
    public boolean checkJobsSuccessOrIgnoreError(String planInstanceId, List<WorkflowJobInfo> jobInfos) {
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
            if (entity.getStatus() == JobStatus.FAILED.status) {
                // 失败的 看是否忽略失败
                WorkflowJobInfo jobInfo = jobInfoMap.get(entity.getJobId());
                if (jobInfo.isTerminateWithFail()) {
                    return false;
                }
            } else if (entity.getStatus() != JobStatus.SUCCEED.status) {
                return false; // 执行中
            }
            // 其他情况为 成功
        }

        return true;
    }

    @Transactional
    public void saveAndScheduleJobInstances(List<JobInstance> jobInstances, LocalDateTime triggerAt) {
        saveJobInstances(jobInstances);
        scheduleJobInstances(jobInstances, triggerAt);
    }

    @Transactional
    public void saveJobInstances(List<JobInstance> jobInstances) {
        if (CollectionUtils.isEmpty(jobInstances)) {
            return;
        }

        // 保存 jobInstance
        List<JobInstanceEntity> jobInstanceEntities = jobInstances.stream().map(DomainConverter::toJobInstanceEntity).collect(Collectors.toList());
        jobInstanceEntityRepo.saveAll(jobInstanceEntities);
        jobInstanceEntityRepo.flush();
    }

    /**
     * 调度jobInstance
     */
    @Transactional
    public void scheduleJobInstances(List<JobInstance> jobInstances, LocalDateTime triggerAt) {
        List<Task> tasks = new ArrayList<>();
        for (JobInstance instance : jobInstances) {
            // 根据job类型创建task
            List<Task> jobTasks;
            JobInfo jobInfo = instance.getJobInfo();
            switch (jobInfo.getType()) {
                case NORMAL:
                    jobTasks = taskFactory.create(instance, TaskType.NORMAL);
                    break;
                case BROADCAST:
                    jobTasks = taskFactory.create(instance, TaskType.BROADCAST);
                    break;
                case MAP:
                case MAP_REDUCE:
                    jobTasks = taskFactory.create(instance, TaskType.SPLIT);
                    break;
                default:
                    throw new JobException(jobInfo.getId(), MsgConstants.UNKNOWN + " job type:" + jobInfo.getType().type);
            }

            // 如果可以创建的任务为空（只有广播任务）
            if (CollectionUtils.isEmpty(jobTasks)) {
                handleJobSuccess(instance);
            } else {
                tasks.addAll(jobTasks);
            }
        }

        saveTasks(tasks, triggerAt);
    }

    @Transactional
    public void saveTasks(List<Task> tasks, LocalDateTime triggerAt) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }

        List<TaskEntity> taskEntities = tasks.stream().map(DomainConverter::toTaskEntity).collect(Collectors.toList());
        taskEntityRepo.saveAll(taskEntities);
        taskEntityRepo.flush();

        ScheduleStrategyContext.waitScheduleTasks(tasks.stream()
                .map(task -> domainConverter.toTaskScheduleTask(task, triggerAt))
                .collect(Collectors.toList())
        );
    }


}
