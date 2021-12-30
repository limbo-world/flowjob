package org.limbo.flowjob.tracker.admin.adapter.config;

import org.limbo.flowjob.tracker.core.job.consumer.*;
import org.limbo.flowjob.tracker.infrastructure.events.ReactorEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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
        AutowireCapableBeanFactory factory = ac.getAutowireCapableBeanFactory();

        // plan 下发
        PlanInfoDispatchConsumer planInfoDispatchConsumer = new PlanInfoDispatchConsumer();
        factory.autowireBean(planInfoDispatchConsumer);
        eventPublisher.subscribe(planInfoDispatchConsumer);

        // task 被接受
        TaskAcceptedConsumer taskAcceptedConsumer = new TaskAcceptedConsumer();
        factory.autowireBean(taskAcceptedConsumer);
        eventPublisher.subscribe(taskAcceptedConsumer);

        // task 被拒绝
        TaskRefusedConsumer taskRefusedConsumer = new TaskRefusedConsumer();
        factory.autowireBean(taskRefusedConsumer);
        eventPublisher.subscribe(taskRefusedConsumer);

        // task 完成
        TaskClosedConsumer taskClosedConsumer = new TaskClosedConsumer();
        factory.autowireBean(taskClosedConsumer);
        eventPublisher.subscribe(taskClosedConsumer);
    }

}
