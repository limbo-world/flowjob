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
public class AcceptedConsumer implements Consumer<JobInstance> {

    private final JobInstanceRepository jobInstanceRepository;

    public AcceptedConsumer(JobInstanceRepository jobInstanceRepository) {
        this.jobInstanceRepository = jobInstanceRepository;
    }


    @Override
    public void accept(JobInstance jobInstance) {
        if (log.isDebugEnabled()) {
            log.debug(jobInstance.getWorkerId() + " accepted " + jobInstance.getId());
        }
        // 由于无法确定先接收到任务执行成功还是任务接收成功的消息，所以可能任务先被直接执行完成了，所以这里需要cas处理
        jobInstanceRepository.compareAndSwapInstanceState(jobInstance.getPlanId(), jobInstance.getPlanInstanceId(),
                jobInstance.getJobId(), JobScheduleStatus.Scheduling, jobInstance.getState());
    }
}
