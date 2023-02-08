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

package org.limbo.flowjob.broker.application.component;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.dispatch.TaskDispatcher;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.SingleJobInstance;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInstance;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.SinglePlan;
import org.limbo.flowjob.broker.core.domain.plan.WorkflowPlan;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.domain.task.TaskFactory;
import org.limbo.flowjob.broker.core.exceptions.JobException;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.TaskScheduleTask;
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
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.constants.TaskType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.dag.DAGNode;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
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

    public static final ThreadLocal<ScheduleStrategyContext> STRATEGY_CONTEXT = new ThreadLocal<>();

    @Transactional
    public void schedule(TriggerType triggerType, Plan plan, LocalDateTime triggerAt) {
        String planId = plan.getPlanId();

        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(plan.getVersion()).orElse(null);
        Verifies.notNull(planInfoEntity, "does not find " + planId + " plan's info by version--" + plan.getVersion() + "");

        PlanEntity planEntity = planEntityRepo.selectForUpdate(planId);
        // 任务是由之前时间创建的 调度时候如果版本改变 可能会有调度时间的变化本次就无需执行
        // 比如 5s 执行一次 分别在 5s 10s 15s 在11s的时候内存里下次执行为 15s 此时修改为 2s 执行一次 那么重新加载plan后应该为 12s 14s 所以15s这次可以跳过
        if (!Objects.equals(plan.getVersion(), planEntity.getCurrentVersion())) {
            log.info("plan:{} version {} change to {}", plan.getPlanId(), plan.getVersion(), planEntity.getCurrentVersion());
            return;
        }

        // 判断是否由当前节点执行
        List<Integer> slots = slotManager.slots();
        if (CollectionUtils.isEmpty(slots)) {
            return;
        }
        PlanSlotEntity planSlotEntity = planSlotEntityRepo.findByPlanId(planId);
        if (planSlotEntity == null) {
            return;
        }
        if (!slots.contains(planSlotEntity.getSlot())) {
            return;
        }

        // 判断并发情况下 是否已经有人提交调度任务 如有则无需处理 防止重复创建数据
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findByPlanIdAndTriggerAtAndTriggerType(
                planId, triggerAt, TriggerType.SCHEDULE.type
        );
        if (planInstanceEntity != null) {
            return;
        }
        // 调度逻辑
        if (PlanType.SINGLE == plan.getType()) {
            savePlanAndScheduleJob(triggerType, (SinglePlan) plan, triggerAt);
        } else {
            savePlanAndScheduleJob(triggerType, (WorkflowPlan) plan, triggerAt);
        }
    }

    private void savePlanAndScheduleJob(TriggerType triggerType, SinglePlan plan, LocalDateTime triggerAt) {
        String planId = plan.getPlanId();
        String version = plan.getVersion();

        // 保存 planInstance
        String planInstanceId = savePlanInstanceEntity(planId, version, triggerType, triggerAt);
        JobInstance jobInstance = newJobInstance(planId, version, plan.planType(), planInstanceId, plan.getJobInfo(), triggerAt);
        scheduleJobInstances(Collections.singletonList(jobInstance), triggerAt);
    }

    protected void savePlanAndScheduleJob(TriggerType triggerType, WorkflowPlan plan, LocalDateTime triggerAt) {
        String planId = plan.getPlanId();
        String version = plan.getVersion();

        // 保存 planInstance
        String planInstanceId = savePlanInstanceEntity(planId, version, triggerType, triggerAt);

        // 获取头部节点
        List<JobInstance> rootJobs = new ArrayList<>();

        for (WorkflowJobInfo jobInfo : plan.getDag().origins()) {
            if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                rootJobs.add(newJobInstance(planId, version, plan.planType(), planInstanceId, new Attributes(), jobInfo, triggerAt));
            }
        }

        // 如果root都为api触发则为空 交由api创建
        if (CollectionUtils.isNotEmpty(rootJobs)) {
            scheduleJobInstances(rootJobs, triggerAt);
        }
    }

    @Transactional
    public void schedule(Task task) {
        if (task.getStatus() != TaskStatus.SCHEDULING) {
            return;
        }

        task.setStatus(TaskStatus.DISPATCHING);
        int num = taskEntityRepo.updateStatusDispatching(task.getTaskId());
        if (num < 1) {
            return; // 可能多个节点操作同个task
        }

        // 下面两个可能会被其他task更新 但是这是正常的
        planInstanceEntityRepo.executing(task.getPlanId(), TimeUtils.currentLocalDateTime());
        jobInstanceEntityRepo.updateStatusExecuting(task.getJobInstanceId());

        boolean dispatched = taskDispatcher.dispatch(task);
        if (dispatched) {
            // 下发成功
            taskEntityRepo.updateStatusExecuting(task.getTaskId(), task.getWorkerId(), TimeUtils.currentLocalDateTime());
        } else {
            // 下发失败
            handleFail(task, MsgConstants.DISPATCH_FAIL, "");
        }
    }

    @Transactional
    public void handleSuccess(Task task, Map<String, Object> context, Object result) {
        // todo v1 更新plan上下文
        String resultJson = result == null ? "" : JacksonUtils.toJSONString(result);
        int num = taskEntityRepo.updateStatusSuccess(task.getTaskId(), TimeUtils.currentLocalDateTime(), resultJson);

        if (num != 1) { // 已经被更新 无需重复处理
            return;
        }
        afterTaskStatusUpdateSuccess(task);
    }

    @Transactional
    public void handleFail(Task task, String errorMsg, String errorStackTrace) {
        int num = taskEntityRepo.updateStatusFail(task.getTaskId(), TimeUtils.currentLocalDateTime(), errorMsg, errorStackTrace);

        if (num != 1) {
            return; // 并发更新过了 正常来说前面job更新成功 这个不可能会进来
        }
        afterTaskStatusUpdateSuccess(task);
    }

    private void afterTaskStatusUpdateSuccess(Task task) {
        JobInstance jobInstance = getJobInstance(task.getJobInstanceId());

        // 判断状态是不是已经更新 可能已经被其它线程处理 正常来说不可能的
        if (JobStatus.EXECUTING != jobInstance.getStatus()) {
            log.warn("task:{} update status success but jobInstance:{} is already changed", task.getTaskId(), task.getJobInstanceId());
            return;
        }
        // 检查task是否都已经完成
        List<TaskEntity> taskEntities = taskEntityRepo.findByJobInstanceIdAndType(task.getJobInstanceId(), task.getType().type);
        for (TaskEntity taskE : taskEntities) {
            if (!TaskStatus.parse(taskE.getStatus()).isCompleted()) {
                return; // 如果还未完成 交由最后完成的task去做后续逻辑处理
            }
        }

        // 如果所有task都是执行成功 则处理成功
        // 如果所有task都是执行失败 则处理失败
        boolean success = taskEntities.stream().allMatch(entity -> TaskStatus.SUCCEED == TaskStatus.parse(entity.getStatus()));
        if (success) {
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
        } else {
            handleJobFail(jobInstance);
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
        if (PlanType.SINGLE == jobInstance.getPlanType()) {
            jobInstanceEntityRepo.updateStatusSuccess(jobInstance.getJobInstanceId());
            planInstanceEntityRepo.success(jobInstance.getPlanInstanceId(), TimeUtils.currentLocalDateTime());
        } else {
            jobInstanceEntityRepo.updateStatusSuccess(jobInstance.getJobInstanceId());

            String planId = jobInstance.getPlanId();
            String version = jobInstance.getPlanVersion();
            String planInstanceId = jobInstance.getPlanInstanceId();
            JobInfo jobInfo = jobInstance.getJobInfo();
            String jobId = jobInfo.getId();

            PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(version).orElse(null);

            DAG<WorkflowJobInfo> dag = domainConverter.toJobDag(planInfoEntity.getJobInfo());
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
                        PlanType planType = PlanType.parse(planInfoEntity.getPlanType());
                        JobInstance subJobInstance = newJobInstance(planId, version, planType, planInstanceId, jobInstance.getContext(), subJobInfo, triggerAt);
                        subJobInstances.add(subJobInstance);
                    }
                }

                if (CollectionUtils.isNotEmpty(subJobInstances)) {
                    scheduleJobInstances(subJobInstances, triggerAt);
                }

            }
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

    private void handleJobFail(JobInstance jobInstance) {
        if (PlanType.WORKFLOW == jobInstance.getPlanType()) {
            WorkflowJobInstance workflowJobInstance = (WorkflowJobInstance) jobInstance;
            WorkflowJobInfo workflowJobInfo = workflowJobInstance.getWorkflowJobInfo();
            if (!workflowJobInfo.isTerminateWithFail()) {
                handleJobSuccess(jobInstance);
                return;
            }
        }

        // job 执行失败 先判断是否需要重试 再做后续处理
        jobInstanceEntityRepo.updateStatusExecuteFail(jobInstance.getJobInstanceId(), MsgConstants.TASK_FAIL);
        if (needRetry(jobInstance)) {
            JobInfo jobInfo = jobInstance.getJobInfo();
            jobInstance.setTriggerAt(TimeUtils.currentLocalDateTime().plusSeconds(jobInfo.getRetryOption().getRetryInterval()));
            jobInstance.setJobInstanceId(null);
            jobInstance.setStatus(JobStatus.SCHEDULING);
            scheduleJobInstances(Collections.singletonList(jobInstance), TimeUtils.currentLocalDateTime());
        } else {
            planInstanceEntityRepo.fail(jobInstance.getPlanInstanceId(), TimeUtils.currentLocalDateTime());
        }
    }

    public boolean needRetry(JobInstance jobInstance) {
        JobInfo jobInfo = jobInstance.getJobInfo();
        // 查询已经失败的记录数
        long retry = jobInstanceEntityRepo.countByPlanInstanceIdAndJobId(jobInstance.getPlanInstanceId(), jobInfo.getId());
        return jobInfo.getRetryOption().getRetry() > retry;
    }

    /**
     * 调度jobInstance
     */
    private void scheduleJobInstances(List<JobInstance> jobInstances, LocalDateTime triggerAt) {
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

    /**
     * 生成新的计划调度记录
     *
     * @param triggerType 触发类型
     * @return 记录id
     */
    private String savePlanInstanceEntity(String planId, String version, TriggerType triggerType, LocalDateTime triggerAt) {
        String planInstanceId = idGenerator.generateId(IDType.PLAN_INSTANCE);
        PlanInstanceEntity planInstanceEntity = new PlanInstanceEntity();
        planInstanceEntity.setPlanInstanceId(planInstanceId);
        planInstanceEntity.setPlanId(planId);
        planInstanceEntity.setPlanInfoId(version);
        planInstanceEntity.setStatus(PlanStatus.SCHEDULING.status);
        planInstanceEntity.setTriggerType(triggerType.type);
        planInstanceEntity.setTriggerAt(triggerAt);
        planInstanceEntity.setContext(JacksonUtils.DEFAULT_NONE_OBJECT);
        planInstanceEntityRepo.saveAndFlush(planInstanceEntity);
        return planInstanceId;
    }

    private void saveTasks(List<Task> tasks, LocalDateTime triggerAt) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }

        List<TaskEntity> taskEntities = tasks.stream().map(domainConverter::toTaskEntity).collect(Collectors.toList());
        taskEntityRepo.saveAll(taskEntities);
        taskEntityRepo.flush();

        List<TaskScheduleTask> scheduleTasks = tasks.stream().map(task -> domainConverter.toTaskScheduleTask(task, triggerAt)).collect(Collectors.toList());

        ScheduleStrategyContext strategyContext = STRATEGY_CONTEXT.get();
        strategyContext.setRequireScheduleTasks(scheduleTasks);
    }

    public JobInstance getJobInstance(String id) {
        JobInstanceEntity entity = jobInstanceEntityRepo.findById(id).orElse(null);
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(entity.getPlanInfoId()).orElse(null);
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(entity.getPlanInstanceId()).orElse(null);

        JobInstance jobInstance;
        PlanType planType = PlanType.parse(planInfoEntity.getPlanType());
        if (PlanType.SINGLE == planType) {
            jobInstance = new SingleJobInstance();
            JobInfo jobInfo = JacksonUtils.parseObject(planInfoEntity.getJobInfo(), JobInfo.class);
            ((SingleJobInstance) jobInstance).setJobInfo(jobInfo);
        } else if (PlanType.WORKFLOW == planType) {
            jobInstance = new WorkflowJobInstance();
            DAG<WorkflowJobInfo> dag = domainConverter.toJobDag(planInfoEntity.getJobInfo());
            ((WorkflowJobInstance) jobInstance).setWorkflowJobInfo(dag.getNode(entity.getJobId()));
        } else {
            throw new IllegalArgumentException("Illegal PlanType in plan:" + planInfoEntity.getPlanId() + " version:" + planInfoEntity.getPlanInfoId());
        }
        Map<String, Object> map = JacksonUtils.parseObject(planInstanceEntity.getContext(), new TypeReference<Map<String, Object>>() {
        });
        wrapJobInstance(jobInstance, entity.getJobInstanceId(), entity.getPlanId(), entity.getPlanInfoId(), planType,
                entity.getPlanInstanceId(), new Attributes(map), entity.getTriggerAt()
        );
        jobInstance.setStatus(JobStatus.parse(entity.getStatus()));
        jobInstance.setStartAt(entity.getStartAt());
        jobInstance.setEndAt(entity.getEndAt());
        return jobInstance;
    }

    public JobInstance newJobInstance(String planId, String planVersion, PlanType planType, String planInstanceId,
                                      Attributes context, WorkflowJobInfo workflowJobInfo, LocalDateTime triggerAt) {
        WorkflowJobInstance instance = new WorkflowJobInstance();
        wrapJobInstance(instance, idGenerator.generateId(IDType.JOB_INSTANCE), planId, planVersion, planType, planInstanceId, context, triggerAt);
        instance.setWorkflowJobInfo(workflowJobInfo);
        return instance;
    }

    public JobInstance newJobInstance(String planId, String planVersion, PlanType planType, String planInstanceId,
                                      JobInfo jobInfo, LocalDateTime triggerAt) {
        SingleJobInstance instance = new SingleJobInstance();
        wrapJobInstance(instance, idGenerator.generateId(IDType.JOB_INSTANCE), planId, planVersion, planType, planInstanceId, new Attributes(), triggerAt);
        instance.setJobInfo(jobInfo);
        return instance;
    }

    public JobInstance wrapJobInstance(JobInstance instance, String jobInstanceId, String planId, String planVersion,
                                       PlanType planType, String planInstanceId, Attributes context, LocalDateTime triggerAt) {
        instance.setJobInstanceId(jobInstanceId);
        instance.setPlanId(planId);
        instance.setPlanInstanceId(planInstanceId);
        instance.setPlanVersion(planVersion);
        instance.setPlanType(planType);
        instance.setStatus(JobStatus.SCHEDULING);
        instance.setTriggerAt(triggerAt);
        instance.setContext(context);
        return instance;
    }

}
