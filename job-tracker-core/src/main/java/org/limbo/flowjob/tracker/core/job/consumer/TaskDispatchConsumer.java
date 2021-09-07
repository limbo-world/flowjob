package org.limbo.flowjob.tracker.core.job.consumer;

import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.Dispatcher;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcherFactory;
import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.job.context.Task;
import org.limbo.flowjob.tracker.core.tracker.WorkerManager;

import java.util.function.Consumer;

/**
 * @author Devil
 * @since 2021/9/7
 */
public class TaskDispatchConsumer implements Consumer<Event<?>> {

    private final JobDispatcherFactory jobDispatcherFactory;

    private final JobInstanceRepository jobInstanceRepository;

    private final WorkerManager workerManager;

    public TaskDispatchConsumer(JobInstanceRepository jobInstanceRepository,
                                WorkerManager workerManager) {
        this.jobDispatcherFactory = new JobDispatcherFactory();
        this.workerManager = workerManager;
        this.jobInstanceRepository = jobInstanceRepository;
    }

    @Override
    public void accept(Event<?> event) {
        if (!(event.getSource() instanceof Task)) {
            return;
        }
        Task task = (Task) event.getSource();
        // todo 下发前确认下对应的jobInstance是否已经关闭
        // 初始化dispatcher
        Dispatcher dispatcher = jobDispatcherFactory.newDispatcher(task.getDispatchOption().getLoadBalanceType());
        if (dispatcher == null) {
            throw new JobExecuteException(task.getJobId(),
                    "Cannot create JobDispatcher for dispatch type: " + task.getDispatchOption().getLoadBalanceType());
        }

        // 订阅下发成功
        task.onAccepted().subscribe(new AcceptedConsumer(jobInstanceRepository));
        // 订阅下发拒绝
        task.onRefused().subscribe(new RefusedConsumer(jobInstanceRepository));

        // 下发任务
        dispatcher.dispatch(task, workerManager.availableWorkers(), Task::startup);
    }
}
