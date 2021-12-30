package org.limbo.flowjob.tracker.core.job.consumer;

import org.limbo.flowjob.tracker.core.evnets.Event;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 *
 * @author Brozen
 * @since 2021-10-13
 */
public abstract class FilterableEventConsumer<T extends Serializable> extends SourceCastEventConsumer<T> {


    private Predicate<Event<?>> filter;

    protected FilterableEventConsumer(Predicate<Event<?>> filter, Class<T> eventSourceClass) {
        super(eventSourceClass);
        setSilentWhenSourceTypeMismatch(true);

        this.filter = filter;
    }

    /**
     * 消费指定类型的时间
     * @param event 事件
     */
    @Override
    public void accept(Event<?> event) {
        if (!filter.test(event)) {
            return;
        }

        super.accept(event);
    }

}
