package org.limbo.flowjob.tracker.core.job.schedule;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.beans.domain.job.Job;
import org.limbo.flowjob.tracker.commons.utils.strategies.Strategy;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleType;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

/**
 * CRON作业调度时间计算器
 *
 * @author Brozen
 * @since 2021-05-21
 */
@Slf4j
public class CronJobScheduleCalculator extends JobScheduleCalculator implements Strategy<Job, Long> {

    /**
     * 作业上下文repository
     */
    private JobContextRepository jobContextRepository;

    protected CronJobScheduleCalculator(JobContextRepository jobContextRepository) {
        super(JobScheduleType.CRON);
        this.jobContextRepository = jobContextRepository;
    }

    /**
     * 通过此策略计算作业的下一次触发时间戳
     * @param job 作业
     * @return 作业下次触发时间戳
     */
    @Override
    public Long apply(Job job) {

        // 未到调度开始时间，不触发下次调度
        Instant nowInstant = Instant.now();
        long startScheduleAt = calculateStartScheduleTimestamp(job);
        if (nowInstant.getEpochSecond() < startScheduleAt) {
            return NO_TRIGGER;
        }

        String cron = job.getScheduleCron();
        try {
            // 校验CRON表达式
            CronExpression.validateExpression(cron);
            CronExpression expression = new CronExpression(cron);

            // 解析下次触发时间
            Date nextSchedule = expression.getNextValidTimeAfter(Date.from(nowInstant));
            if (nextSchedule == null) {
                return NO_TRIGGER;
            }

            return nextSchedule.getTime();
        } catch (ParseException e) {
            log.error("parse cron expression {} failed!", cron, e);
            return NO_TRIGGER;
        }

    }

}
