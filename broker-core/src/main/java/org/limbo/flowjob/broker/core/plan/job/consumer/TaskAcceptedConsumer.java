package org.limbo.flowjob.broker.core.plan.job.consumer;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventTags;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.repositories.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.TaskRepository;

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
        if (log.isDebugEnabled()) {
            log.debug("{} accepted task {}", task.getWorkerId(), task);
        }

        // 由于无法确定先接收到任务执行成功还是任务接收成功的消息，所以可能任务先被直接执行完成了，所以这里需要cas处理
        jobInstanceRepo.execute(task.getJobInstanceId());
        taskRepo.execute(task.getTaskId());
    }
}
