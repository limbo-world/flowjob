/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.plan.job.consumer;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.core.broker.TrackerNode;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventPublisher;
import org.limbo.flowjob.broker.core.events.EventTags;
import org.limbo.flowjob.broker.core.exceptions.JobExecuteException;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.plan.PlanInstanceContext;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.JobDAG;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstance;
import org.limbo.flowjob.broker.core.plan.job.context.JobRecord;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.plan.job.handler.JobFailHandler;
import org.limbo.flowjob.broker.core.repositories.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.JobRecordRepository;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceContextRepository;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanRepository;
import org.limbo.flowjob.broker.core.repositories.TaskRepository;
import org.limbo.flowjob.broker.core.utils.TimeUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2021/8/16
 */
@Slf4j
public class TaskClosedConsumer extends FilterTagEventConsumer<Task> {

    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepository;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceContextRepository planInstanceContextRepository;

    @Setter(onMethod_ = @Inject)
    private JobRecordRepository jobRecordRepository;

    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepository;

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    @Setter(onMethod_ = @Inject)
    private TrackerNode trackerNode;

    @Setter(onMethod_ = @Inject)
    private EventPublisher<Event<?>> eventPublisher;

    public TaskClosedConsumer() {
        super(EventTags.TASK_CLOSED, Task.class);
    }


    /**
     * {@inheritDoc}
     * @param event 指定泛型类型的事件
     */
    @Override
    protected void consumeEvent(Event<Task> event) {
        Task task = event.getSource();
        switch (task.getResult()) {
            case SUCCEED:
                handlerSuccess(task);
                break;

            case FAILED:
                handlerFailed(task);
                break;
        }
    }


    public void handlerSuccess(Task task) {
        handlerShardingTaskSuccess();
        handlerNormalTaskSuccess(task);
    }


    public void handlerShardingTaskSuccess() {
        // todo 分页任务 从返回值获取分出来的task并下发
    }


    public void handlerNormalTaskSuccess(Task task) {
        // 判断是否所有 task 执行过
        Task.ID taskId = task.getId();
        if (taskRepository.countUnclosed(taskId) > 0) {
            return;
        }

        // 结束 job
        jobInstanceRepository.end(taskId.idOfJobInstance(), JobScheduleStatus.SUCCEED);
        jobRecordRepository.end(taskId.idOfJobRecord(), JobScheduleStatus.SUCCEED);

        PlanInstance planInstance = planInstanceRepository.get(taskId.idOfPlanRecord());
        JobDAG dag = planInstance.getDag();
        List<Job> subJobs = dag.getSubJobs(taskId.jobId);
        if (CollectionUtils.isEmpty(subJobs)) {

            // 无后续节点，需要判断是否plan结束
            if (checkJobsFinished(taskId.idOfPlanInstance(), dag.jobs())) {
                // 结束plan
                planInstanceContextRepository.end(taskId.idOfPlanInstance(), PlanScheduleStatus.SUCCEED);
                planInstanceRepository.end(taskId.idOfPlanRecord(), PlanScheduleStatus.SUCCEED);

                // 判断 plan 是否需要 重新调度 只有 FIXED_INTERVAL类型需要反馈，让任务在时间轮里面能重新下发，手动的和其他的都不需要
                Plan plan = planRepository.getByVersion(taskId.planId, taskId.planInfoId);
                if (ScheduleType.FIXED_INTERVAL == plan.getInfo().getScheduleOption().getScheduleType()
                        && planInstance.isManual()) {
                    plan.setLastScheduleAt(planInstance.getStartAt());
                    plan.setLastFeedbackAt(TimeUtil.nowInstant());
                    trackerNode.jobTracker().schedule(plan);
                }
            }

        } else {

            // 不为end节点，继续下发
            for (Job job : subJobs) {
                if (checkJobsFinished(taskId.idOfPlanInstance(), dag.getPreJobs(job.getJobId()))) {
                    JobRecord jobRecord = job.newRecord(taskId.idOfPlanInstance(), JobScheduleStatus.SCHEDULING);
                    eventPublisher.publish(new Event<>(jobRecord));
                }
            }

        }
    }

    public void handlerFailed(Task task) {
        // 更新task状态
        taskRepository.executed(task);

        Task.ID taskId = task.getId();
        PlanInstanceContext planInstanceContext = planInstanceContextRepository.get(taskId.idOfPlanInstance());

        // 重复处理的情况
        if (planInstanceContext.getState() == PlanScheduleStatus.SUCCEED
                || planInstanceContext.getState() == PlanScheduleStatus.FAILED) {
            return;
        }

        jobInstanceRepository.end(taskId.idOfJobInstance(), JobScheduleStatus.FAILED);

        JobRecord jobRecord = jobRecordRepository.get(taskId.idOfJobRecord());
        List<JobInstance> jobInstances = jobInstanceRepository.listByRecord(taskId.idOfJobRecord());
        // 判断是否超过重试次数
        if (jobRecord.getRetry() >= CollectionUtils.size(jobInstances)) {
            eventPublisher.publish(new Event<>(jobRecord));
            return;
        }

        jobRecordRepository.end(taskId.idOfJobRecord(), JobScheduleStatus.FAILED);

        // 超过重试次数 执行handler
        JobFailHandler failHandler = jobRecord.getFailHandler();
        try {
            failHandler.handle();
            handlerSuccess(task);
        } catch (JobExecuteException e) {
            // 此次 planInstance 失败
            planInstanceContextRepository.end(taskId.idOfPlanInstance(), PlanScheduleStatus.FAILED);
            List<PlanInstanceContext> planInstanceContexts = planInstanceContextRepository.list(taskId.idOfPlanRecord());

            PlanInstance planInstance = planInstanceRepository.get(taskId.idOfPlanRecord());
            // 判断是否超过重试次数
            if (planInstance.getRetry() >= CollectionUtils.size(planInstanceContexts)) {
                eventPublisher.publish(new Event<>(planInstance));
                return;
            }

            // 如果超过重试次数 此planRecord失败
            planInstanceRepository.end(taskId.idOfPlanRecord(), PlanScheduleStatus.FAILED);
        }

    }

    /**
     * 判断一批job是否完成可以执行下一步
     */
    public boolean checkJobsFinished(PlanInstanceContext.ID planInstanceId, List<Job> jobs) {
        // 获取db中 job实例
        List<String> jobIds = jobs.stream().map(Job::getJobId).collect(Collectors.toList());
        List<JobRecord> jobRecords = jobRecordRepository.getRecords(planInstanceId, jobIds);

        // 有实例还未创建直接返回
        if (jobs.size() > jobRecords.size()) {
            return false;
        }

        // 判断是否所有实例都可以触发下个任务
        for (JobRecord jobRecord : jobRecords) {
            if (!jobRecord.canTriggerNext()) {
                return false;
            }
        }

        return true;
    }

}
