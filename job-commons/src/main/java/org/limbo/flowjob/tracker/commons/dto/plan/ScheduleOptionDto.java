package org.limbo.flowjob.tracker.commons.dto.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Schema(title = "作业计划调度配置参数")
public class ScheduleOptionDto {

    /**
     * 作业调度方式
     */
    @Schema(title = "作业调度方式", implementation = Integer.class, description = ScheduleType.DESCRIPTION)
    private ScheduleType scheduleType;

    /**
     * 作业调度开始时间，从此时间开始执行调度。
     */
    @Schema(title = "作业调度开始时间")
    private LocalDateTime scheduleStartAt;

    /**
     * 作业延迟时间。
     * 当调度方式为{@link ScheduleType#DELAYED}或{@link ScheduleType#FIXED_INTERVAL}时，
     * 表示从<code>scheduleStartAt</code>时间点开始，延迟多久触发作业调度。
     */
    @Schema(title = "作业延迟时间",
            implementation = Float.class,
            description = "当调度方式为DELAYED或FIXED_INTERVAL时，表示从scheduleStartAt时间点开始，延迟多久触发作业调度。"
    )
    private Duration scheduleDelay;

    /**
     * 作业调度间隔时间。
     * 当调度方式为{@link ScheduleType#FIXED_INTERVAL}时，表示前一次作业调度执行完成后，隔多久触发第二次调度。
     * 当调度方式为{@link ScheduleType#FIXED_RATE}时，表示前一次作业调度下发后，隔多久触发第二次调度。
     */
    @Schema(title = "获取作业调度间隔时间",
            implementation = Float.class,
            description = "当调度方式为FIXED_INTERVAL时，表示前一次作业调度执行完成后，隔多久触发第二次调度。"
                    + "当调度方式为FIXED_RATE时，表示前一次作业调度下发后，隔多久触发第二次调度。"
    )
    private Duration scheduleInterval;

    /**
     * 作业调度的CRON表达式
     * 当调度方式为{@link ScheduleType#CRON}时，根据此CRON表达式计算得到的时间点触发作业调度。
     */
    @Schema(title = "作业调度的CRON表达式", description = "当调度方式为CRON时，根据此CRON表达式计算得到的时间点触发作业调度。")
    private String scheduleCron;

    /**
     * 重试次数
     */
    @Schema(title = "重试次数")
    private Integer retry;
}
