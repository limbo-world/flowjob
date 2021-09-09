package org.limbo.flowjob.tracker.core.job.consumer;

import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.evnets.EventPublisher;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanRecord;
import org.limbo.flowjob.tracker.core.plan.PlanRecordRepository;

import java.util.function.Consumer;

/**
 * @author Devil
 * @since 2021/9/7
 */
public class PlanDispatchConsumer implements Consumer<Event<?>> {

    private final PlanRecordRepository planRecordRepository;

    private final EventPublisher<Event<?>> eventPublisher;

    public PlanDispatchConsumer(PlanRecordRepository planRecordRepository, EventPublisher<Event<?>> eventPublisher) {
        this.planRecordRepository = planRecordRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void accept(Event<?> event) {
        if (!(event.getSource() instanceof Plan)) {
            return;
        }
        Plan plan = (Plan) event.getSource();
        Long planRecordId = planRecordRepository.createId(plan.getPlanId());
        PlanRecord planRecord = plan.newRecord(planRecordId, PlanScheduleStatus.SCHEDULING, false);
        planRecordRepository.add(planRecord);
        eventPublisher.publish(new Event<>(planRecord));
    }
}
