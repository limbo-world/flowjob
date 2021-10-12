package org.limbo.flowjob.tracker.core.job.consumer;

import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.evnets.EventPublisher;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.context.JobRecord;
import org.limbo.flowjob.tracker.core.job.context.JobRecordRepository;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.tracker.core.plan.PlanInstanceRepository;
import org.limbo.flowjob.tracker.core.plan.PlanRecord;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Devil
 * @since 2021/9/7
 */
public class PlanRecordDispatchConsumer implements Consumer<Event<?>> {

    private final PlanInstanceRepository planInstanceRepository;

    private final JobRecordRepository jobRecordRepository;

    private final EventPublisher<Event<?>> eventPublisher;

    public PlanRecordDispatchConsumer(PlanInstanceRepository planInstanceRepository,
                                      JobRecordRepository jobRecordRepository,
                                      EventPublisher<Event<?>> eventPublisher) {
        this.planInstanceRepository = planInstanceRepository;
        this.jobRecordRepository = jobRecordRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void accept(Event<?> event) {
        if (!(event.getSource() instanceof PlanRecord)) {
            return;
        }
        PlanRecord planRecord = (PlanRecord) event.getSource();
        Integer planInstanceId = planInstanceRepository.createId(planRecord.getPlanId(), planRecord.getPlanRecordId());
        PlanInstance planInstance = planRecord.newInstance(planInstanceId, PlanScheduleStatus.SCHEDULING);
        planInstanceRepository.add(planInstance);

        List<Job> jobs = planRecord.getDag().getEarliestJobs();
        for (Job job : jobs) {
            JobRecord jobRecord = job.newRecord(planInstance.getPlanId(), planInstance.getPlanRecordId(), planInstanceId, JobScheduleStatus.SCHEDULING);
            jobRecordRepository.add(jobRecord);
            eventPublisher.publish(new Event<>(jobRecord));
        }
    }
}
