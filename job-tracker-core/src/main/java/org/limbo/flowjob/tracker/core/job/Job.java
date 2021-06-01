package org.limbo.flowjob.tracker.core.job;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.JobDispatchType;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleType;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 作业轻量级领域对象，包括job的属性。
 *
 * @author Brozen
 * @since 2021-05-28
 */
@Data
public class Job {

    /**
     * 作业ID
     */
    private String id;

    /**
     * 所需的CPU核心数，小于等于0表示此作业未定义CPU需求。在分发作业时，会根据此方法返回的CPU核心需求数量来检测一个worker是否有能力执行此作业。
     */
    private float cpuRequirement;

    /**
     * 所需的内存GB数，小于等于0表示此作业未定义内存需求。在分发作业时，会根据此方法返回的内存需求数量来检测一个worker是否有能力执行此作业。
     */
    private float ramRequirement;

    /**
     * 作业调度方式
     */
    private JobScheduleType scheduleType;

    /**
     * 作业分发方式
     */
    private JobDispatchType dispatchType;

    /**
     * 获取作业延迟时间。
     * 当调度方式为{@link JobScheduleType#DELAYED}或{@link JobScheduleType#FIXED_INTERVAL}时，
     * 表示从<code>createdAt</code>时间点开始，延迟多久触发作业调度。
     */
    private Duration scheduleDelay;

    /**
     * 获取作业调度间隔时间。
     * 当调度方式为{@link JobScheduleType#FIXED_INTERVAL}时，表示前一次作业调度执行完成后，隔多久触发第二次调度。
     * 当调度方式为{@link JobScheduleType#FIXED_RATE}时，表示前一次作业调度下发后，隔多久触发第二次调度。
     */
    private Duration scheduleInterval;

    /**
     * 作业调度的CRON表达式
     * 当调度方式为{@link JobScheduleType#CRON}时，根据此CRON表达式计算得到的时间点触发作业调度。
     */
    private String scheduleCron;

    /**
     * 作业创建时间
     */
    private LocalDateTime createdAt;

}
