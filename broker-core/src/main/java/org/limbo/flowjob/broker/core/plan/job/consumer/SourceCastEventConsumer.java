package org.limbo.flowjob.broker.core.plan.job.consumer;

import lombok.Setter;
import org.limbo.flowjob.broker.core.events.Event;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * @author Brozen
 * @since 2021-12-30
 */
public abstract class SourceCastEventConsumer<T extends Serializable> implements Consumer<Event<?>> {

    /**
     * 需要消费的事件 source 类型
     */
    private final Class<T> eventSourceClass;

    /**
     * 当事件source类型与给出的类型不匹配时，是否静默处理事件。
     * 如配置为ture，则事件处理直接返回；
     * 如配置为false，会抛出{@link ClassCastException}
     */
    @Setter
    private boolean silentWhenSourceTypeMismatch = true;

    protected SourceCastEventConsumer(Class<T> eventSourceClass) {
        this.eventSourceClass = eventSourceClass;
    }


    /**
     * 消费指定类型的时间
     * @param event 事件
     */
    @Override
    public void accept(Event<?> event) {
        Serializable source = event.getSource();
        if (source == null || !eventSourceClass.isAssignableFrom(source.getClass())) {
            if (!silentWhenSourceTypeMismatch) {
                throw new ClassCastException(source == null ? "null" : source.getClass().getName()
                        + " cannot cast to " + eventSourceClass.getName());
            }
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
