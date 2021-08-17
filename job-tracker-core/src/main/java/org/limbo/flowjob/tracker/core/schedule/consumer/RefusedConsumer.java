package org.limbo.flowjob.tracker.core.schedule.consumer;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;

import java.util.function.Consumer;

/**
 * @author Devil
 * @since 2021/8/16
 */
@Slf4j
public class RefusedConsumer implements Consumer<JobInstance> {

    private final JobInstanceRepository jobInstanceRepository;

    public RefusedConsumer(JobInstanceRepository jobInstanceRepository) {
        this.jobInstanceRepository = jobInstanceRepository;
    }


    @Override
    public void accept(JobInstance jobInstance) {
        if (log.isDebugEnabled()) {
            log.debug(jobInstance.getWorkerId() + " refused " + jobInstance.getId());
        }
        jobInstanceRepository.compareAndSwapInstanceState(jobInstance.getPlanId(), jobInstance.getPlanInstanceId(),
                jobInstance.getJobId(), JobScheduleStatus.Scheduling, jobInstance.getState());
    }
}
