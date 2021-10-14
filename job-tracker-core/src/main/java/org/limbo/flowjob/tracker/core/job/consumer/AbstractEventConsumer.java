package org.limbo.flowjob.tracker.core.job.consumer;

import org.limbo.flowjob.tracker.core.evnets.Event;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 *
 * @param <T> 需要消费的事件 source 类型
 * @author Brozen
 * @since 2021-10-13
 */
public abstract class AbstractEventConsumer<T extends Serializable> implements Consumer<Event<?>> {

    /**
     * 需要消费的事件 source 类型
     */
    private final Class<T> consumeEventSourceClass;

    protected AbstractEventConsumer(Class<T> consumeEventSourceClass) {
        this.consumeEventSourceClass = consumeEventSourceClass;
    }

    /**
     * 消费指定类型的时间
     * @param event 事件
     */
    @Override
    public void accept(Event<?> event) {
        Serializable source = event.getSource();
        if (source == null || !consumeEventSourceClass.isAssignableFrom(source.getClass())) {
            return;
        }

        @SuppressWarnings("unchecked")
        Event<T> castedEvent = (Event<T>) event;
        consumeEvent(castedEvent);
    }


    /**
     * 处理带类型的事件
     * @param event 指定泛型类型的事件
     */
    protected abstract void consumeEvent(Event<T> event);

}
