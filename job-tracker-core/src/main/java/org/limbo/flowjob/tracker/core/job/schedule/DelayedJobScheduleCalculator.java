package org.limbo.flowjob.tracker.core.job.schedule;

import org.limbo.flowjob.tracker.commons.beans.domain.job.Job;
import org.limbo.flowjob.tracker.commons.beans.domain.job.JobContext;
import org.limbo.flowjob.tracker.commons.utils.strategies.Strategy;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleType;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 固定延迟作业调度时间计算器
 *
 * @author Brozen
 * @since 2021-05-21
 */
public class DelayedJobScheduleCalculator extends JobScheduleCalculator implements Strategy<Job, Long> {

    /**
     * 作业上下文repository
     */
    private JobContextRepository jobContextRepository;

    protected DelayedJobScheduleCalculator(JobContextRepository jobContextRepository) {
        super(JobScheduleType.DELAYED);
        this.jobContextRepository = jobContextRepository;
    }

    /**
     * 通过此策略计算作业的下一次触发时间戳
     * @param job 作业
     * @return 作业下次触发时间戳
     */
    @Override
    public Long apply(Job job) {
        // 只调度一次
        JobContext latestContext = jobContextRepository.getLatestContext(job.getId());
        if (latestContext != null) {
            return NO_TRIGGER;
        }

        // 从创建时间开始，间隔固定delay进行调度
        LocalDateTime createdAt = job.getCreatedAt();
        Duration delay = job.getScheduleDelay();
        long triggerAt = createdAt.toEpochSecond(ZoneOffset.UTC);
        triggerAt = delay != null ? triggerAt + delay.toMillis() : triggerAt;

        long now = Instant.now().getEpochSecond();
        return Math.max(triggerAt, now);
    }

}
