package org.limbo.flowjob.tracker.core.job;

import lombok.Setter;
import org.limbo.flowjob.tracker.core.job.schedule.JobScheduleCalculator;
import org.limbo.utils.UUIDUtils;

/**
 * @author Brozen
 * @since 2021-05-24
 */
public abstract class AbstractJob implements Job {

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

    public AbstractJob(JobScheduleCalculator triggerCalculator,
                       JobContextRepository jobContextRepository) {
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
