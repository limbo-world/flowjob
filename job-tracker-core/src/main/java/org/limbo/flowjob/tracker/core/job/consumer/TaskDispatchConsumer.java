package org.limbo.flowjob.tracker.core.job.consumer;

import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.Dispatcher;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.job.context.*;
import org.limbo.flowjob.tracker.core.tracker.WorkerManager;

import java.util.function.Consumer;

/**
 * @author Devil
 * @since 2021/9/7
 */
public class TaskDispatchConsumer implements Consumer<Event<?>> {

    private final JobDispatcherFactory jobDispatcherFactory;

    private final JobRecordRepository jobRecordRepository;

    private final JobInstanceRepository jobInstanceRepository;

    private final TaskRepository taskRepository;

    private final WorkerManager workerManager;

    public TaskDispatchConsumer(JobRecordRepository jobRecordRepository, JobInstanceRepository jobInstanceRepository,
                                TaskRepository taskRepository, WorkerManager workerManager) {
        this.jobDispatcherFactory = new JobDispatcherFactory();
        this.workerManager = workerManager;
        this.jobRecordRepository = jobRecordRepository;
        this.jobInstanceRepository = jobInstanceRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void accept(Event<?> event) {
        if (!(event.getSource() instanceof TaskInfo)) {
            return;
        }
        TaskInfo taskInfo = (TaskInfo) event.getSource();
        // todo 根据下发类型 单机 广播 分片
        switch (taskInfo.getType()) {
            case NORMAL:
                // 直接创建
            case SHARDING:
                // 创建一个分片任务
            case BROADCAST:
                // 根据 worker 创建广播任务
            default:
                // todo 未知的类型 如果直接返回的话，job如何结束，这个其实和没有task是同个逻辑，需要考虑一下
                return;
        }

        // todo 下发前确认下对应的jobInstance是否已经关闭
        // 初始化dispatcher
        Dispatcher dispatcher = jobDispatcherFactory.newDispatcher(task.getDispatchOption().getLoadBalanceType());
        if (dispatcher == null) {
            throw new JobExecuteException(task.getJobId(),
                    "Cannot create JobDispatcher for dispatch type: " + task.getDispatchOption().getLoadBalanceType());
        }

        // 订阅下发成功
        task.onAccepted().subscribe(new AcceptedConsumer(jobRecordRepository, jobInstanceRepository, taskRepository));
        // 订阅下发拒绝
        task.onRefused().subscribe(new RefusedConsumer(jobInstanceRepository));

        // 下发任务
        dispatcher.dispatch(task, workerManager.availableWorkers(), Task::startup);
    }
}
