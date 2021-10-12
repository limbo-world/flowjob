package org.limbo.flowjob.tracker.core.job.consumer;

import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.evnets.EventPublisher;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.job.context.JobRecord;
import org.limbo.flowjob.tracker.core.job.context.TaskInfo;

import java.util.function.Consumer;

/**
 * @author Devil
 * @since 2021/9/7
 */
public class JobRecordDispatchConsumer implements Consumer<Event<?>> {

    private final JobInstanceRepository jobInstanceRepository;

    private final EventPublisher<Event<?>> eventPublisher;

    public JobRecordDispatchConsumer(JobInstanceRepository jobInstanceRepository,
                                     EventPublisher<Event<?>> eventPublisher) {
        this.jobInstanceRepository = jobInstanceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void accept(Event<?> event) {
        if (!(event.getSource() instanceof JobRecord)) {
            return;
        }
        JobRecord jobRecord = (JobRecord) event.getSource();
        Integer jobInstanceId = jobInstanceRepository.createId(jobRecord.getPlanId(), jobRecord.getPlanRecordId(), jobRecord.getPlanInstanceId(), jobRecord.getJobId());
        JobInstance jobInstance = jobRecord.newInstance(jobInstanceId, JobScheduleStatus.SCHEDULING);
        jobInstanceRepository.add(jobInstance);
        TaskInfo taskInfo = jobInstance.taskInfo();
        eventPublisher.publish(new Event<>(taskInfo));
    }
}
