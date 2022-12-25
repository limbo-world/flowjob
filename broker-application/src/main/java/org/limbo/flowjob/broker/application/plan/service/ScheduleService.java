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

package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.core.dispatch.TaskDispatcher;
import org.limbo.flowjob.broker.core.domain.job.JobFactory;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanFactory;
import org.limbo.flowjob.broker.core.domain.plan.PlanInfo;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.domain.task.TaskFactory;
import org.limbo.flowjob.broker.core.exceptions.JobException;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.service.IScheduleService;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.constants.ExecuteResult;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.constants.TaskType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.dag.DAGNode;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 调度相关业务
 *
 * @author KaiFengCai
 * @since 2022/12/10
 */
@Slf4j
@Service
public class ScheduleService implements IScheduleService {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private TaskFactory taskFactory;

    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepository;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;

    @Setter(onMethod_ = @Inject)
    private MetaTaskScheduler metaTaskScheduler;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepository;

    @Setter(onMethod_ = @Inject)
    private JobFactory jobFactory;

    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanFactory planFactory;

    @Override
    @Transactional
    public void schedule(Plan plan) {
        PlanInfo planInfo = plan.getInfo();
        if (planInfo.getScheduleOption().getScheduleType() == null) {
            log.error("{} scheduleType is null info={}", plan.scheduleId(), planInfo);
            return;
        }
        if (ScheduleType.UNKNOWN == planInfo.getScheduleOption().getScheduleType()) {
            log.error("{} scheduleType is {} info={}", plan.scheduleId(), MsgConstants.UNKNOWN, planInfo);
            return;
        }
        if (ScheduleType.NONE == planInfo.getScheduleOption().getScheduleType()) {
            return;
        }

        String planId = plan.getPlanId();
        // 加锁
        PlanEntity planEntity = planEntityRepo.selectForUpdate(planId);
        if (!Objects.equals(plan.getCurrentVersion(), planEntity.getCurrentVersion())) {
            log.info("{} version {} change to {}", plan.scheduleId(), plan.getCurrentVersion(), planEntity.getCurrentVersion());
            return;
        }

        PlanInstance planInstance = planFactory.newInstance(planInfo, TriggerType.SCHEDULE, plan.getTriggerAt());

        // 判断并发情况下 是否已经有人提交调度任务 如有则无需处理 防止重复创建数据
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findByPlanIdAndTriggerAtAndTriggerType(
                planId, planInstance.getTriggerAt(), TriggerType.SCHEDULE.type
        );
        if (planInstanceEntity != null) {
            return;
        }

        // 保存 planInstance
        planInstanceRepository.save(planInstance);

        // 获取头部节点
        List<JobInstance> rootJobs = new ArrayList<>();

        for (JobInfo jobInfo : planInstance.getDag().origins()) {
            if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                rootJobs.add(jobFactory.newInstance(planInstance, jobInfo, TimeUtils.currentLocalDateTime()));
            }
        }

        // 如果root都为api触发则为空 交由api创建
        if (CollectionUtils.isNotEmpty(rootJobs)) {
            scheduleJobInstances(rootJobs);
        }
    }

    @Override
    @Transactional
    public void schedule(Task task) {
        int num = taskEntityRepo.updateStatusDispatching(task.getTaskId());
        if (num < 1) {
            return; // 可能多个节点操作同个task
        }

        // 下面两个可能会被其他task更新 但是这是正常的
        planInstanceEntityRepo.executing(task.getPlanId(), TimeUtils.currentLocalDateTime());
        jobInstanceEntityRepo.updateStatusExecuting(task.getJobInstanceId());

        // todo 判断是否有 workId 广播 会已经存在 其他应该在这里获取
        TaskDispatcher.dispatch(task);

        if (TaskStatus.FAILED == task.getStatus()) {
            // 下发失败
            handleTaskFail(task.getTaskId(), MsgConstants.DISPATCH_FAIL, "");
        } else {
            // 下发成功
            taskEntityRepo.updateStatusExecuting(task.getTaskId(), task.getWorkerId(), TimeUtils.currentLocalDateTime());
        }
    }

    /**
     * 调度jobInstance
     */
    @Transactional
    public void scheduleJobInstances(List<JobInstance> jobInstances) {

        // 保存 jobInstance
        jobInstanceRepository.saveAll(jobInstances);

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

            // 如果可以创建的任务为空（一般为广播任务）则需要判断是终止plan还是继续下发后续job
            if (CollectionUtils.isEmpty(jobTasks)) {
                // job 是否终止流程
                if (instance.isTerminateWithFail()) {
                    jobInstanceEntityRepo.updateStatusExecuteFail(instance.getJobInstanceId(), MsgConstants.EMPTY_TASKS);
                } else {
                    PlanInstance planInstance = planInstanceRepository.get(instance.getPlanInstanceId());
                    scheduleNextJobInstance(planInstance, instance.getJobId());
                }
            } else {
                tasks.addAll(jobTasks);
            }
        }

        taskRepository.saveAll(tasks);

        for (Task task : tasks) {
            try {
                metaTaskScheduler.schedule(task);
            } catch (Exception e) {
                // 调度失败 不要影响事务，事务提交后 由task的状态检查任务去修复task的执行情况
                log.error("task schedule fail! task={}", task, e);
            }
        }

    }

    /**
     * 下发后续任务
     */
    @Transactional
    public void scheduleNextJobInstance(PlanInstance planInstance, String jobId) {
        DAG<JobInfo> dag = planInstance.getDag();
        // 当前节点的子节点
        List<JobInfo> subJobInfos = dag.subNodes(jobId);

        if (CollectionUtils.isEmpty(subJobInfos)) {
            // 当前节点为叶子节点 检测 Plan 实例是否已经执行完成
            // 1. 所有节点都已经成功或者失败 2. 这里只关心plan的成功更新，失败是在task回调
            if (checkJobsSuccessOrIgnoreError(planInstance.getPlanInstanceId(), dag.lasts())) {
                planInstanceEntityRepo.success(planInstance.getPlanInstanceId(), TimeUtils.currentLocalDateTime());
            }
        } else {

            // 后续作业存在，则检测是否可触发，并继续下发作业
            List<JobInstance> subJobInstances = new ArrayList<>();
            for (JobInfo subJobInfo : subJobInfos) {
                // 前置节点已经完成则可以下发
                if (checkJobsSuccessOrIgnoreError(planInstance.getPlanInstanceId(), dag.preNodes(subJobInfo.getId()))) {
                    subJobInstances.add(jobFactory.newInstance(planInstance, subJobInfo, TimeUtils.currentLocalDateTime()));
                }
            }

            if (CollectionUtils.isNotEmpty(subJobInstances)) {
                scheduleJobInstances(subJobInstances);
            }

        }
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

    /**
     * Worker任务执行反馈
     *
     * @param taskId 任务id
     * @param param  反馈参数
     */
    @Transactional
    public void taskFeedback(String taskId, TaskFeedbackParam param) {
        ExecuteResult result = ExecuteResult.parse(param.getResult());

        if (log.isDebugEnabled()) {
            log.debug("receive task feedback id:{} result:{}", taskId, result);
        }

        switch (result) {
            case SUCCEED:
                handleTaskSuccess(taskId, param);
                break;

            case FAILED:
                handleTaskFail(taskId, param.getErrorMsg(), param.getErrorStackTrace());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");

            default:
                throw new IllegalStateException("Unexpect execute result: " + param.getResult());
        }
    }

    @Transactional
    public void handleTaskSuccess(String taskId, TaskFeedbackParam param) {
        // todo 更新plan上下文

        int num = taskEntityRepo.updateStatusSuccess(taskId, TimeUtils.currentLocalDateTime(), JacksonUtils.toJSONString(param.getResultAttributes()));

        if (num != 1) { // 已经被更新 无需重复处理
            return;
        }
        handleTask(taskId);
    }

    @Transactional
    @Override
    public void handleTaskFail(String taskId, String errorMsg, String errorStackTrace) {
        int num = taskEntityRepo.updateStatusFail(taskId, TimeUtils.currentLocalDateTime(), errorMsg, errorStackTrace);

        if (num != 1) {
            return; // 并发更新过了 正常来说前面job更新成功 这个不可能会进来
        }
        handleTask(taskId);
    }

    //    @Transactional
    private void handleTask(String taskId) {
        // 加锁 ---- 移除 前面 task更新的时候已经有锁 失败的不会进来
//        JobInstanceEntity jobInstanceEntity = jobInstanceEntityRepo.selectForUpdate(jobInstanceId);

        TaskEntity taskEntity = taskEntityRepo.findById(taskId).get();

        JobInstance jobInstance = jobInstanceRepository.get(taskEntity.getJobInstanceId());
        // 判断状态是不是已经更新 可能已经被其它线程处理 正常来说不可能的
        if (JobStatus.EXECUTING != jobInstance.getStatus()) {
            log.warn("task:{} update status success but jobInstance:{} is already changed", taskId, taskEntity.getJobInstanceId());
            return;
        }
        // 检查task是否都已经完成
        List<TaskEntity> taskEntities = taskEntityRepo.findByJobInstanceIdAndType(taskEntity.getJobInstanceId(), taskEntity.getType());
        for (TaskEntity taskE : taskEntities) {
            if (!TaskStatus.parse(taskE.getStatus()).isCompleted()) {
                return; // 如果还未完成 交由最后完成的task去做后续逻辑处理
            }
        }

        // 如果所有task都是执行成功 则处理成功
        // 如果所有task都是执行失败 则处理失败
        boolean success = taskEntities.stream().allMatch(entity -> TaskStatus.SUCCEED == TaskStatus.parse(entity.getStatus()));
        if (success) {
            handleJobSuccess(taskEntity, jobInstance);
        } else {
            handleJobFail(jobInstance);
        }

    }

    //    @Transactional
    private void handleJobSuccess(TaskEntity taskEntity, JobInstance jobInstance) {
        switch (jobInstance.getType()) {
            case NORMAL:
            case BROADCAST:
                handleJobSuccessFinal(jobInstance);
                break;
            case MAP:
                handleMapJobSuccess(taskEntity, jobInstance);
                break;
            case MAP_REDUCE:
                handleMapReduceJobSuccess(taskEntity, jobInstance);
                break;
            default:
                throw new IllegalArgumentException(MsgConstants.UNKNOWN + " JobType in jobInstance:" + jobInstance.getJobInstanceId());
        }
    }

    private void handleMapJobSuccess(TaskEntity taskEntity, JobInstance jobInstance) {
        TaskType taskType = TaskType.parse(taskEntity.getType());
        switch (taskType) {
            case SPLIT:
                createAndScheduleTask(jobInstance, TaskType.MAP);
                break;
            case MAP:
                handleJobSuccessFinal(jobInstance);
                break;
            default:
                throw new IllegalArgumentException("Illegal TaskType in task:" + taskEntity.getTaskId());
        }
    }

    private void handleMapReduceJobSuccess(TaskEntity taskEntity, JobInstance jobInstance) {
        TaskType taskType = TaskType.parse(taskEntity.getType());
        switch (taskType) {
            case SPLIT:
                createAndScheduleTask(jobInstance, TaskType.MAP);
                break;
            case MAP:
                createAndScheduleTask(jobInstance, TaskType.REDUCE);
                break;
            case REDUCE:
                handleJobSuccessFinal(jobInstance);
                break;
            default:
                throw new IllegalArgumentException("Illegal TaskType in task:" + taskEntity.getTaskId());
        }
    }

    private void createAndScheduleTask(JobInstance instance, TaskType taskType) {
        List<Task> tasks = taskFactory.create(instance, instance.getTriggerAt(), taskType);
        taskRepository.saveAll(tasks);
        for (Task task : tasks) {
            try {
                metaTaskScheduler.schedule(task);
            } catch (Exception e) {
                // 调度失败 不要影响事务，事务提交后 由task的状态检查任务去修复task的执行情况
                log.error("task schedule fail! task={}", task, e);
            }
        }
    }

    private void handleJobSuccessFinal(JobInstance jobInstance) {
        jobInstanceEntityRepo.updateStatusSuccess(jobInstance.getJobInstanceId());

        PlanInstance planInstance = planInstanceRepository.get(jobInstance.getPlanInstanceId());
        scheduleNextJobInstance(planInstance, jobInstance.getJobId());
    }

    //    @Transactional
    private void handleJobFail(JobInstance jobInstance) {
        if (jobInstance.isTerminateWithFail()) {

            jobInstanceEntityRepo.updateStatusExecuteFail(jobInstance.getJobInstanceId(), MsgConstants.TASK_FAIL);

            if (jobInstance.retry()) {
                scheduleJobInstances(Collections.singletonList(jobInstance));
            } else {
                planInstanceEntityRepo.fail(jobInstance.getPlanInstanceId(), TimeUtils.currentLocalDateTime());
            }
        } else {
            handleJobSuccessFinal(jobInstance);
        }
    }

}
