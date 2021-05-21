package org.limbo.flowjob.tracker.core.job.schedule;

import org.limbo.flowjob.tracker.core.commons.Strategy;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.JobScheduleType;

/**
 * @author Brozen
 * @since 2021-05-21
 */
public class FixDelayJobTriggerCalculator extends JobTriggerCalculator implements Strategy<Job, Long> {

    protected FixDelayJobTriggerCalculator() {
        super(JobScheduleType.DELAY);
    }


    /**
     * 通过此策略计算作业的下一次触发时间戳
     * @param job 作业
     * @return 作业下次触发时间戳
     */
    @Override
    public Long apply(Job job) {
        // TODO
        return null;
    }

}
