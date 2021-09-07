package org.limbo.flowjob.tracker.core.job.consumer;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.core.job.context.Task;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;

import java.util.function.Consumer;

/**
 * @author Devil
 * @since 2021/8/16
 */
@Slf4j
public class RefusedConsumer implements Consumer<Task> {

    private final JobInstanceRepository jobInstanceRepository;

    public RefusedConsumer(JobInstanceRepository jobInstanceRepository) {
        this.jobInstanceRepository = jobInstanceRepository;
    }


    @Override
    public void accept(Task task) {
        if (log.isDebugEnabled()) {
            log.debug(task.getWorkerId() + " refused " + task.getId());
        }
        // todo 拒绝应该重试
//        jobInstanceRepository.compareAndSwapInstanceState(task.getPlanId(), task.getPlanInstanceId(),
//                task.getJobId(), JobScheduleStatus.Scheduling, task.getState());
    }
}
