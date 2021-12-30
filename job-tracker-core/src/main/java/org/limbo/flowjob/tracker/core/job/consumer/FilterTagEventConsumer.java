package org.limbo.flowjob.tracker.core.job.consumer;

import org.limbo.flowjob.tracker.core.evnets.Event;

import java.io.Serializable;

/**
 * @author Brozen
 * @since 2021-12-30
 */
public abstract class FilterTagEventConsumer<T extends Serializable> extends FilterableEventConsumer<T> {

    /**
     * 用入参正则表达式去匹配{@link Event#getTag()}，若能成功匹配则消费事件。
     * @param tagPattern 正则
     */
    public FilterTagEventConsumer(String tagPattern, Class<T> eventSourceClass) {
        super(e -> e.getTag().matches(tagPattern), eventSourceClass);
    }

}
