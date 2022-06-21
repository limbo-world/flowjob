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
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.broker.WorkerManager;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventPublisher;
import org.limbo.flowjob.broker.core.exceptions.JobExecuteException;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstance;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.plan.job.context.TaskCreateStrategyFactory;
import org.limbo.flowjob.broker.core.plan.job.context.TaskInfo;
import org.limbo.flowjob.broker.core.repositories.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.TaskRepository;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Devil
 * @since 2021/9/7
 */
public class PlanInfoDispatchConsumer extends SourceCastEventConsumer<PlanInfo> {

    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planRecordRepo;

    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepo;

    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepo;

    @Setter(onMethod_ = @Inject)
    private TaskCreateStrategyFactory taskCreateStrategyFactory;

    @Setter(onMethod_ = @Inject)
    private WorkerSelectorFactory workerSelectorFactory;

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
        // todo 这里应该直接由plan生成 由planInstance管理其生命周期
        PlanInfo planInfo = event.getSource();

        // 先生成执行计划实例
        PlanInstance planInstance = planInfo.newInstance(PlanScheduleStatus.SCHEDULING, false);
        String planInstanceId = planRecordRepo.add(planInstance);
        planInstance.setPlanInstanceId(planInstanceId);

        // 生成作业执行记录
        List<Job> jobs = planInstance.getDag().getEarliestJobs();
        for (Job job : jobs) {
            JobInstance jobInstance = job.newInstance(planInfo.getPlanId(), planInstanceId, JobScheduleStatus.SCHEDULING);
            String jobInstanceId = jobInstanceRepo.add(jobInstance);
            jobInstance.setJobInstanceId(jobInstanceId);

            // 将作业对应的任务信息下发给worker
            dispatchTask(jobInstance.newTask(job));
        }

        eventPublisher.publish(new Event<>(planInstance));
    }


    /**
     * 执行任务下发
     * @param taskInfo 待下发的任务信息
     */
    private void dispatchTask(TaskInfo taskInfo) {
        TaskCreateStrategyFactory.TaskCreateStrategy taskCreateStrategy = taskCreateStrategyFactory.newStrategy(taskInfo.getType());

        // todo 下发前确认下对应的jobInstance是否已经关闭
        // 初始化dispatcher
        WorkerSelector workerSelector = workerSelectorFactory.newSelector(taskInfo.getDispatchOption().getLoadBalanceType());
        if (workerSelector == null) {
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
        workerSelector.select(task, workerManager.availableWorkers());
    }

}
