package org.limbo.flowjob.broker.core.plan.job.consumer;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventTags;
import org.limbo.flowjob.broker.core.plan.job.context.Task;

/**
 * @author Brozen
 * @since 2021-10-20
 */
@Slf4j
public class TaskRefusedConsumer extends FilterTagEventConsumer<Task> {

    public TaskRefusedConsumer() {
        super(EventTags.TASK_REFUSED, Task.class);
    }


    /**
     * {@inheritDoc}
     * @param event 指定泛型类型的事件
     */
    @Override
    protected void consumeEvent(Event<Task> event) {
        Task task = event.getSource();
        if (log.isDebugEnabled()) {
            log.debug("worker[{}] refused task[{}]", task.getWorkerId(), task.getId());
        }

        // todo 拒绝应该重试
    }

}
