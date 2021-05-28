package org.limbo.flowjob.tracker.core.job;

import lombok.Setter;
import org.limbo.flowjob.tracker.core.executor.dispatcher.JobDispatchType;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.core.job.context.JobContextStatus;
import org.limbo.flowjob.tracker.core.job.context.SimpleJobContext;
import org.limbo.flowjob.tracker.core.job.schedule.JobScheduleCalculator;
import org.limbo.utils.UUIDUtils;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2021-05-24
 */
public class SimpleJob extends Job {

    /**
     * 作业触发计算器
     */
    @Setter
    private JobScheduleCalculator triggerCalculator;

    /**
     * 上下文repository
     */
    @Setter
    private JobContextRepository jobContextRepository;

    public SimpleJob(String id, float cpuRequirement, float ramRequirement,
                     JobScheduleType scheduleType, JobDispatchType dispatchType,
                     Duration scheduleDelay, Duration scheduleInterval,
                     String scheduleCron, LocalDateTime createdAt,
                     JobScheduleCalculator triggerCalculator,
                     JobContextRepository jobContextRepository) {
        super(id, cpuRequirement, ramRequirement, scheduleType,
                dispatchType, scheduleDelay, scheduleInterval, scheduleCron, createdAt);

        this.triggerCalculator = triggerCalculator;
        this.jobContextRepository = jobContextRepository;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public long nextTriggerAt() {
        return triggerCalculator.apply(this);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public JobContext newContext() {
        SimpleJobContext context = new SimpleJobContext(getId(), UUIDUtils.randomID(),
                JobContextStatus.INIT, null, jobContextRepository);
        jobContextRepository.addContext(context);
        return context;
    }

    /**
     * {@inheritDoc}
     * @param contextId 上下文ID
     * @return
     */
    @Override
    public JobContext getContext(String contextId) {
        return jobContextRepository.getContext(getId(), contextId);
    }

}
