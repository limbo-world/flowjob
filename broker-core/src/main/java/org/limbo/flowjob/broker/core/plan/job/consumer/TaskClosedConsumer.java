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
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.plan.PlanScheduler;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.JobDAG;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstance;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.plan.job.handler.JobFailHandler;
import org.limbo.flowjob.broker.core.repositories.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanSchedulerRepository;
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
    private JobInstanceRepository jobInstanceRepository;

    @Setter(onMethod_ = @Inject)
    private PlanSchedulerRepository planSchedulerRepository;

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

    /**
     * 分片task执行成功处理
     */
    public void handlerShardingTaskSuccess() {
        // todo 分页任务 从返回值获取分出来的task并下发
    }

    /**
     * 普通task执行成功处理
     */
    public void handlerNormalTaskSuccess(Task task) {
        // 如果之前已经由提交过task成功 就无需处理 防止重复下发任务
        // todo 这个干嘛的 和查询结果有点不对
//        if (taskRepository.countByStates(task.getJobInstanceId(), Lists.newArrayList(TaskScheduleStatus.FEEDBACK)) > 0) {
//            return;
//        }

        // todo task 状态处理

        // 结束 job
        boolean ended = jobInstanceRepository.end(task.getJobInstanceId(), JobScheduleStatus.SUCCEED);
        if (!ended) {
            return; // 更新失败 说明已经被更新了 直接跳过
        }

        PlanInstance planInstance = planInstanceRepository.get(task.getPlanInstanceId());
        JobDAG dag = planInstance.getDag();
        List<Job> subJobs = dag.getSubJobs(task.getJobId());
        if (CollectionUtils.isEmpty(subJobs)) {

            // 无后续节点，需要判断是否plan结束
            if (checkJobsFinished(task.getPlanInstanceId(), dag.jobs())) {
                // 结束plan
                planInstanceRepository.end(task.getPlanInstanceId(), PlanScheduleStatus.SUCCEED);

                // 判断 plan 是否需要 重新调度 只有 FIXED_INTERVAL类型需要反馈，让任务在时间轮里面能重新下发，手动的和其他的都不需要
                PlanScheduler planScheduler = planSchedulerRepository.get(planInstance.getVersion());
                if (ScheduleType.FIXED_INTERVAL == planScheduler.getInfo().getScheduleOption().getScheduleType()
                        && planInstance.isManual()) {
                    planScheduler.setLastScheduleAt(planInstance.getStartAt());
                    planScheduler.setLastFeedbackAt(TimeUtil.nowInstant());
                    trackerNode.jobTracker().schedule(planScheduler);
                }
            }

        } else {

            // 不为end节点，继续下发
            for (Job job : subJobs) {
                if (checkJobsFinished(task.getPlanInstanceId(), dag.getPreJobs(job.getJobId()))) {
                    JobInstance jobInstance = job.newInstance(task.getPlanId(), task.getPlanInstanceId(), JobScheduleStatus.SCHEDULING);
                    eventPublisher.publish(new Event<>(jobInstance));
                }
            }

        }
    }

    /**
     * task执行失败处理
     */
    public void handlerFailed(Task task) {
        // 更新task状态
        taskRepository.executed(task.getTaskId());

        // todo task 执行end

        PlanInstance planInstance = planInstanceRepository.get(task.getPlanInstanceId());

        // 重复处理的情况
        if (planInstance.getState() == PlanScheduleStatus.SUCCEED
                || planInstance.getState() == PlanScheduleStatus.FAILED) {
            return;
        }

        JobInstance jobInstance = jobInstanceRepository.get(task.getJobInstanceId());
        // todo 如何判断
        Integer taskNum = 0; // = taskRepository.countByStates(task.getJobInstanceId());
        // 如果没有超过重试次数则新下发一个任务
        if (jobInstance.getRetry() >= taskNum) {
            eventPublisher.publish(new Event<>(jobInstance));
            return;
        }

        jobInstanceRepository.end(task.getJobInstanceId(), JobScheduleStatus.FAILED);

        // 超过重试次数 执行handler
        JobFailHandler failHandler = jobInstance.getFailHandler();
        try {
            failHandler.handle();
            handlerSuccess(task);
        } catch (JobExecuteException e) {
            // 此次 planInstance 失败
            planInstanceRepository.end(task.getPlanInstanceId(), PlanScheduleStatus.FAILED);
        }

    }

    /**
     * 判断一批job是否完成可以执行下一步
     */
    public boolean checkJobsFinished(String planInstanceId, List<Job> jobs) {
        // 获取db中 job实例
        List<String> jobIds = jobs.stream().map(Job::getJobId).collect(Collectors.toList());
        List<JobInstance> jobInstances = jobInstanceRepository.getInstances(planInstanceId, jobIds);

        // 有实例还未创建直接返回
        if (jobs.size() > jobInstances.size()) {
            return false;
        }

        // 判断是否所有实例都可以触发下个任务
        for (JobInstance jobInstance : jobInstances) {
            if (!jobInstance.canTriggerNext()) {
                return false;
            }
        }

        return true;
    }

}
