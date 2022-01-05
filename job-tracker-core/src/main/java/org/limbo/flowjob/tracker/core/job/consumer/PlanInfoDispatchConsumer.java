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

package org.limbo.flowjob.tracker.core.job.consumer;

import lombok.Setter;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.Dispatcher;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.evnets.EventPublisher;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.context.*;
import org.limbo.flowjob.tracker.core.plan.*;
import org.limbo.flowjob.tracker.core.tracker.WorkerManager;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Devil
 * @since 2021/9/7
 */
public class PlanInfoDispatchConsumer extends SourceCastEventConsumer<PlanInfo> {

    @Setter(onMethod_ = @Inject)
    private PlanRecordRepository planRecordRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepo;

    @Setter(onMethod_ = @Inject)
    private JobRecordRepository jobRecordRepos;

    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepo;

    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepo;

    @Setter(onMethod_ = @Inject)
    private TaskCreateStrategyFactory taskCreateStrategyFactory;

    @Setter(onMethod_ = @Inject)
    private JobDispatcherFactory jobDispatcherFactory;

    @Setter(onMethod_ = @Inject)
    private WorkerManager workerManager;

    @Setter(onMethod_ = @Inject)
    private EventPublisher<Event<?>> eventPublisher;

    public PlanInfoDispatchConsumer() {
        super(PlanInfo.class);
    }


    /**
     * {@inheritDoc}
     * @param event 指定泛型类型的事件
     */
    @Override
    protected void consumeEvent(Event<PlanInfo> event) {
        PlanInfo planInfo = event.getSource();

        // 先生成执行计划记录
        PlanRecord.ID planRecordId = planRecordRepo.createId(planInfo.getPlanId());
        PlanRecord planRecord = planInfo.newRecord(planRecordId, PlanScheduleStatus.SCHEDULING, false);
        planRecordRepo.add(planRecord);

        // 生成执行计划实例
        PlanInstance.ID planInstanceId = planInstanceRepo.createId(planRecord.getId());
        PlanInstance planInstance = planRecord.newInstance(planInstanceId, PlanScheduleStatus.SCHEDULING);
        planInstanceRepo.add(planInstance);

        // 生成作业执行记录
        List<Job> jobs = planRecord.getDag().getEarliestJobs();
        for (Job job : jobs) {
            JobRecord jobRecord = job.newRecord(
                    planInstanceId,
                    JobScheduleStatus.SCHEDULING
            );
            jobRecordRepos.add(jobRecord);

            // 每个作业执行记录生成作业实例
            JobInstance.ID jobInstanceId = jobInstanceRepo.createId(jobRecord.getId());
            JobInstance jobInstance = jobRecord.newInstance(jobInstanceId, JobScheduleStatus.SCHEDULING);
            jobInstanceRepo.add(jobInstance);

            // 将作业对应的任务信息下发给worker
            dispatchTask(jobInstance.taskInfo(job));
        }

        eventPublisher.publish(new Event<>(planRecord));
    }


    /**
     * 执行任务下发
     * @param taskInfo 待下发的任务信息
     */
    private void dispatchTask(TaskInfo taskInfo) {
        TaskCreateStrategyFactory.TaskCreateStrategy taskCreateStrategy = taskCreateStrategyFactory.newStrategy(taskInfo.getType());

        // todo 下发前确认下对应的jobInstance是否已经关闭
        // 初始化dispatcher
        Dispatcher dispatcher = jobDispatcherFactory.newDispatcher(taskInfo.getDispatchOption().getLoadBalanceType());
        if (dispatcher == null) {
            throw new JobExecuteException(taskInfo.getJobId(),
                    "Cannot create JobDispatcher for dispatch type: " + taskInfo.getDispatchOption().getLoadBalanceType());
        }

        // 保存Task
        Task task = taskCreateStrategy.apply(taskInfo);
        taskRepo.add(task);

        // FIXME 这里使用全局监听器，不临时注册
        // 订阅下发成功
//        task.onAccepted().subscribe(new AcceptedConsumer(jobRecordRepository, jobInstanceRepository, taskRepository));
        // 订阅下发拒绝
//        task.onRefused().subscribe(new RefusedConsumer(jobInstanceRepository));

        // 下发任务
        dispatcher.dispatch(task, workerManager.availableWorkers(), Task::startup);
    }

}
