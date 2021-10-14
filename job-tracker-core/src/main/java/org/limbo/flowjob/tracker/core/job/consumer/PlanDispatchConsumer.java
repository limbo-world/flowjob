package org.limbo.flowjob.tracker.core.job.consumer;

import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.evnets.EventPublisher;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanRecord;
import org.limbo.flowjob.tracker.core.plan.PlanRecordRepository;

/**
 * @author Devil
 * @since 2021/9/7
 */
public class PlanDispatchConsumer extends AbstractEventConsumer<Plan> {

    private final PlanRecordRepository planRecordRepository;

    private final EventPublisher<Event<?>> eventPublisher;

    public PlanDispatchConsumer(PlanRecordRepository planRecordRepository, EventPublisher<Event<?>> eventPublisher) {
        super(Plan.class);
        this.planRecordRepository = planRecordRepository;
        this.eventPublisher = eventPublisher;
    }


    /**
     * {@inheritDoc}
     * @param event 指定泛型类型的事件
     */
    @Override
    protected void consumeEvent(Event<Plan> event) {
        Plan plan = event.getSource();
        Long planRecordId = planRecordRepository.createId(plan.getPlanId());
        PlanRecord planRecord = plan.newRecord(planRecordId, PlanScheduleStatus.SCHEDULING, false);
        planRecordRepository.add(planRecord);
        eventPublisher.publish(new Event<>(planRecord));
    }

}
