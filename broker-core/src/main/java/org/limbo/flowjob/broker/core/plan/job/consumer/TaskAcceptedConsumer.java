package org.limbo.flowjob.broker.core.plan.job.consumer;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventTags;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstanceRepository;
import org.limbo.flowjob.broker.core.plan.job.context.JobRecordRepository;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.plan.job.context.TaskRepository;

import javax.inject.Inject;

/**
 * @author Brozen
 * @since 2021-10-19
 */
@Slf4j
public class TaskAcceptedConsumer extends FilterTagEventConsumer<Task> {

    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepo;

    @Setter(onMethod_ = @Inject)
    private JobRecordRepository jobRecordRepo;

    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepo;


    public TaskAcceptedConsumer() {
        super(EventTags.TASK_ACCEPTED, Task.class);
    }

    /**
     * {@inheritDoc}
     * @param event 指定泛型类型的事件
     */
    @Override
    protected void consumeEvent(Event<Task> event) {
        Task task = event.getSource();
        Task.ID taskId = task.getId();
        if (log.isDebugEnabled()) {
            log.debug("{} accepted task {}", task.getWorkerId(), taskId);
        }

        // 由于无法确定先接收到任务执行成功还是任务接收成功的消息，所以可能任务先被直接执行完成了，所以这里需要cas处理
        jobInstanceRepo.execute(taskId.idOfJobInstance());
        jobRecordRepo.execute(taskId.idOfJobRecord());
        taskRepo.execute(taskId);
    }
}
