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
import org.limbo.flowjob.broker.application.plan.component.TaskScheduler;
import org.limbo.flowjob.broker.core.dispatch.TaskDispatcher;
import org.limbo.flowjob.broker.core.domain.job.JobFactory;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.domain.task.TaskFactory;
import org.limbo.flowjob.broker.core.exceptions.JobException;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
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
import org.limbo.flowjob.common.exception.VerifyException;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.dag.DAGNode;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 调度相关业务
 *
 * @author KaiFengCai
 * @since 2022/12/10
 */
@Slf4j
@Service
public class ScheduleService {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private TaskFactory taskFactory;

    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepository;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;

    @Setter(onMethod_ = @Inject)
    private TaskScheduler taskScheduler;

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

    /**
     * 调度PlanInstance
     * @param planInstance
     */
    @Transactional
    public void schedulePlanInstance(PlanInstance planInstance) {
        // 保存数据
        planInstance.trigger();

        // 获取头部数据
        List<JobInstance> rootJobs = new ArrayList<>();

        for (JobInfo jobInfo : planInstance.getDag().origins()) {
            if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                rootJobs.add(jobFactory.newInstance(planInstance, jobInfo, TimeUtils.currentLocalDateTime()));
            }
        }

        // rootJobs 可能为空 因为可能根节点都是非调度触发
        saveScheduleInfo(planInstance, rootJobs);

        // 执行调度逻辑
        for (JobInstance instance : rootJobs) {
            scheduleJobInstance(instance);
        }
    }

    private void saveScheduleInfo(PlanInstance planInstance, List<JobInstance> rootJobs) {
        String planId = planInstance.getPlanId();

        // 加锁
        planEntityRepo.selectForUpdate(planId);

        // 判断并发情况下 是否已经有人提交调度任务 如有则无需处理 防止重复创建数据
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo
                .findByPlanIdAndExpectTriggerAtAndTriggerType(
                        planId, planInstance.getExpectTriggerAt(), TriggerType.SCHEDULE.type
                );
        if (planInstanceEntity != null) {
            return;
        }

        // 保存 planInstance
        planInstanceRepository.save(planInstance);

        // 保存 jobInstance
        jobInstanceRepository.saveAll(rootJobs);

        // 更新plan的下次触发时间
        if (ScheduleType.FIXED_DELAY != planInstance.getScheduleOption().getScheduleType() && TriggerType.SCHEDULE == planInstance.getScheduleOption().getTriggerType()) {
            planEntityRepo.nextTriggerAt(planId, planInstance.nextTriggerAt());
        }
    }

    /**
     * 调度jobInstance
     * @param instance
     */
    @Transactional
    public void scheduleJobInstance(JobInstance instance) {
        // 更新 job 为执行中
        int num = jobInstanceEntityRepo.updateStatusExecuting(instance.getJobInstanceId());

        if (num != 1) {
            return;
        }

        // 根据job类型创建task
        List<Task> tasks;
        switch (instance.getType()) {
            case NORMAL:
                tasks = taskFactory.create(instance, TaskType.NORMAL);
                break;
            case BROADCAST:
                tasks = taskFactory.create(instance, TaskType.BROADCAST);
                break;
            case MAP:
            case MAP_REDUCE:
                tasks = taskFactory.create(instance, TaskType.SPLIT);
                break;
            default:
                throw new JobException(instance.getJobId(), MsgConstants.UNKNOWN + " job type:" + instance.getType().type);
        }

        // 如果可以创建的任务为空（一般为广播任务）则需要判断是终止plan还是继续下发后续job
        if (CollectionUtils.isEmpty(tasks)) {
            // job 是否终止流程
            if (instance.isTerminateWithFail()) {
                jobInstanceEntityRepo.updateStatusExecuteFail(instance.getJobInstanceId(), MsgConstants.EMPTY_TASKS);
            } else {
                PlanInstance planInstance = planInstanceRepository.get(instance.getPlanInstanceId());
                scheduleNextJobInstance(planInstance, instance.getJobId());
            }
        } else {

            taskRepository.saveAll(tasks);

            for (Task task : tasks) {
                try {
                    taskScheduler.schedule(task);
                } catch (Exception e) {
                    // 调度失败 不要影响事务，事务提交后 由task的状态检查任务去修复task的执行情况
                    log.error("task schedule fail! task={}", task);
                }
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
                jobInstanceRepository.saveAll(subJobInstances);

                for (JobInstance subJobInstance : subJobInstances) {
                    scheduleJobInstance(subJobInstance); // 这里递归了会不会性能不太好
                }
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

    @Transactional
    public void scheduleTask(Task task) {
        // 判断是否有workid 广播 会已经存在 其他应该在这里获取

        TaskDispatcher.dispatch(task);

        if (TaskStatus.FAILED == task.getStatus()) {
            // 下发失败
            handleTaskFail(task.getTaskId(), task.getJobInstanceId(), MsgConstants.DISPATCH_FAIL, "");
        } else {
            // 下发成功
            taskEntityRepo.updateStatusExecuting(task.getTaskId(), task.getWorkerId());
        }
    }

    /**
     * Worker任务执行反馈
     *
     * @param taskId 任务id
     * @param param  反馈参数
     */
    @Transactional
    public void taskFeedback(String taskId, TaskFeedbackParam param) {
        // 获取实例
        TaskEntity task = taskEntityRepo.findById(taskId)
                .orElseThrow(() -> new VerifyException("Task not exist! id:" + taskId));

        ExecuteResult result = ExecuteResult.parse(param.getResult());

        if (log.isDebugEnabled()) {
            log.debug("receive task feedback id:{} result:{}", taskId, result);
        }

        switch (result) {
            case SUCCEED:
                handleTaskSuccess(task.getTaskId(), task.getJobInstanceId(), param);
                break;

            case FAILED:
                handleTaskFail(task.getTaskId(), task.getJobInstanceId(), param.getErrorMsg(), param.getErrorStackTrace());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");

            default:
                throw new IllegalStateException("Unexpect execute result: " + param.getResult());
        }
    }

    @Transactional
    public void handleTaskSuccess(String taskId, String jobInstanceId, TaskFeedbackParam param) {
        // todo 更新plan上下文

        int num = taskEntityRepo.updateStatusSuccess(taskId, JacksonUtils.toJSONString(param.getResultAttributes()));

        if (num != 1) { // 已经被更新 无需重复处理
            return;
        }
        handleJobStatus(jobInstanceId);
    }

    @Transactional
    public void handleTaskFail(String taskId, String jobInstanceId, String errorMsg, String errorStackTrace) {
        int num = taskEntityRepo.updateStatusFail(taskId, errorMsg, errorStackTrace);

        if (num != 1) {
            return; // 并发更新过了 正常来说前面job更新成功 这个不可能会进来
        }
        handleJobStatus(jobInstanceId);
    }

    //    @Transactional
    private void handleJobStatus(String jobInstanceId) {
        // 加锁
        jobInstanceEntityRepo.selectForUpdate(jobInstanceId);
        // 检查task是否都已经完成
        List<TaskEntity> taskEntities = taskEntityRepo.findByJobInstanceId(jobInstanceId);
        for (TaskEntity taskEntity : taskEntities) {
            if (!TaskStatus.parse(taskEntity.getStatus()).isCompleted()) {
                return; // 如果还未完成 交由最后完成的task去做后续逻辑处理
            }
        }

        JobInstance jobInstance = jobInstanceRepository.get(jobInstanceId);
        // 判断状态是不是已经更新 可能已经被其它线程处理
        if (JobStatus.EXECUTING != jobInstance.getStatus()) {
            return;
        }

        // 如果所有task都是执行成功 则处理成功
        // 如果所有task都是执行失败 则处理失败
        boolean success = taskEntities.stream().allMatch(entity -> TaskStatus.SUCCEED == TaskStatus.parse(entity.getStatus()));
        if (success) {
            handleJobSuccess(jobInstance);
        } else {
            handleJobFail(jobInstance);
        }

    }

    //    @Transactional
    private void handleJobSuccess(JobInstance jobInstance) {
        jobInstanceEntityRepo.updateStatusSuccess(jobInstance.getJobInstanceId());

        PlanInstance planInstance = planInstanceRepository.get(jobInstance.getPlanInstanceId());
        scheduleNextJobInstance(planInstance, jobInstance.getJobId());
    }

    //    @Transactional
    private void handleJobFail(JobInstance jobInstance) {
        if (jobInstance.isTerminateWithFail()) {

            jobInstanceEntityRepo.updateStatusExecuteFail(jobInstance.getJobInstanceId(), MsgConstants.TASK_FAIL);

            if (jobInstance.retry()) {
                jobInstanceRepository.save(jobInstance);
                scheduleJobInstance(jobInstance);
            } else {
                planInstanceEntityRepo.fail(jobInstance.getPlanInstanceId(), TimeUtils.currentLocalDateTime());
            }
        } else {
            handleJobSuccess(jobInstance);
        }
    }

}
