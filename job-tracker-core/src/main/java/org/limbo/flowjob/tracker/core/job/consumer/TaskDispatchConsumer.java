package org.limbo.flowjob.tracker.core.job.consumer;

import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.commons.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.tracker.core.dispatcher.WorkerSelectorFactory;
import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.job.context.*;
import org.limbo.flowjob.tracker.core.tracker.WorkerManager;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;

/**
 * @author Devil
 * @since 2021/9/7
 */
public class TaskDispatchConsumer extends AbstractEventConsumer<TaskInfo> {

    private final WorkerSelectorFactory workerSelectorFactory;

    private final JobRecordRepository jobRecordRepository;

    private final JobInstanceRepository jobInstanceRepository;

    private final TaskRepository taskRepository;

    private final WorkerManager workerManager;

    public TaskDispatchConsumer(
            JobRecordRepository jobRecordRepository,
            JobInstanceRepository jobInstanceRepository,
            TaskRepository taskRepository,
            WorkerManager workerManager
    ) {
        super(TaskInfo.class);
        this.workerSelectorFactory = new WorkerSelectorFactory();
        this.workerManager = workerManager;
        this.jobRecordRepository = jobRecordRepository;
        this.jobInstanceRepository = jobInstanceRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * {@inheritDoc}
     * @param event 指定泛型类型的事件
     */
    @Override
    protected void consumeEvent(Event<TaskInfo> event) {

        TaskInfo taskInfo = event.getSource();
        // todo 根据下发类型 单机 广播 分片
        // todo 下发前确认下对应的jobInstance是否已经关闭
        switch (taskInfo.getType()) {
            case NORMAL:
                // 单节点任务
                normal();
                break;
            case SHARDING:
                // 创建一个分片任务
                sharding();
                break;
            case BROADCAST:
                // 根据 worker 创建广播任务
                broadcast();
                break;
            default:
                // 未知的类型 正常来说不可能保存成功
                break;
        }
    }

    public void normal() {
        Task task = new Task(); // todo
        WorkerSelector workerSelector = workerSelectorFactory.newSelector(task.getDispatchOption().getLoadBalanceType());
        if (workerSelector == null) {
            throw new JobExecuteException(task.getJobId(),
                    "Cannot create JobDispatcher for dispatch type: " + task.getDispatchOption().getLoadBalanceType());
        }

        // 订阅下发成功
        task.onAccepted().subscribe(new AcceptedConsumer(jobRecordRepository, jobInstanceRepository, taskRepository));
        // 订阅下发拒绝
        task.onRefused().subscribe(new RefusedConsumer(jobInstanceRepository));

        // 下发任务

        // todo worker 拒绝后的重试
        Worker worker = workerSelector.select(task, workerManager.availableWorkers());
        if (worker == null) {
            throw new JobWorkerException(task.getJobId(), null, "No worker available!");
        }

        task.startup(worker);
    }

    public void sharding() {
        // 分片后 根据worker随机下发
    }

    /**
     * 广播模式，广播所有存活节点
     */
    public void broadcast() {
        for (Worker availableWorker : workerManager.availableWorkers()) {
            Task task = new Task(); // todo

            // 订阅下发成功
            task.onAccepted().subscribe(new AcceptedConsumer(jobRecordRepository, jobInstanceRepository, taskRepository));
            // 订阅下发拒绝
            task.onRefused().subscribe(new RefusedConsumer(jobInstanceRepository));

            // todo worker 拒绝后的重试
            task.startup(availableWorker);
        }
    }

}
