package org.limbo.flowjob.tracker.admin.adapter.config;

import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.job.consumer.PlanInfoDispatchConsumer;
import org.limbo.flowjob.tracker.core.job.consumer.TaskAcceptedConsumer;
import org.limbo.flowjob.tracker.core.job.consumer.TaskClosedConsumer;
import org.limbo.flowjob.tracker.core.job.consumer.TaskRefusedConsumer;
import org.limbo.flowjob.tracker.infrastructure.events.ReactorEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

/**
 * 解决循环依赖的中转bean
 *
 * @author Devil
 * @since 2021/9/10
 */
@Component
public class EventPublisherConsumerRegister {

    @Autowired
    private ApplicationContext ac;

    @Autowired
    private ReactorEventPublisher eventPublisher;

    @PostConstruct
    public void subscribe() {
        // plan 下发
        injectAndSubscribe(new PlanInfoDispatchConsumer());

        // task 被接受
        injectAndSubscribe(new TaskAcceptedConsumer());

        // task 被拒绝
        injectAndSubscribe(new TaskRefusedConsumer());

        // task 完成
        injectAndSubscribe(new TaskClosedConsumer());
    }

    private void injectAndSubscribe(Consumer<Event<?>> consumer) {
        AutowireCapableBeanFactory factory = ac.getAutowireCapableBeanFactory();
        // 注入bean信息
        factory.autowireBean(consumer);
        eventPublisher.subscribe(consumer);
    }


}
