package org.limbo.flowjob.tracker.commons.dto.plan;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @date 2021/7/14 4:54 下午
 */
@Data
public class ScheduleOptionDto {
    /**
     * 作业调度方式
     */
    private ScheduleType scheduleType;

    /**
     * 作业调度开始时间，从此时间开始执行调度。
     */
    private LocalDateTime scheduleStartAt;

    /**
     * 获取作业延迟时间。
     * 当调度方式为{@link ScheduleType#DELAYED}或{@link ScheduleType#FIXED_INTERVAL}时，
     * 表示从<code>scheduleStartAt</code>时间点开始，延迟多久触发作业调度。
     */
    private Duration scheduleDelay;

    /**
     * 获取作业调度间隔时间。
     * 当调度方式为{@link ScheduleType#FIXED_INTERVAL}时，表示前一次作业调度执行完成后，隔多久触发第二次调度。
     * 当调度方式为{@link ScheduleType#FIXED_RATE}时，表示前一次作业调度下发后，隔多久触发第二次调度。
     */
    private Duration scheduleInterval;

    /**
     * 作业调度的CRON表达式
     * 当调度方式为{@link ScheduleType#CRON}时，根据此CRON表达式计算得到的时间点触发作业调度。
     */
    private String scheduleCron;
}
