package org.limbo.flowjob.tracker.core.job;

import org.limbo.flowjob.tracker.core.executor.dispatcher.JobDispatchType;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 作业定义，作业的一些基本属性getter
 *
 * @author Brozen
 * @since 2021-05-24
 */
public interface JobDefinition {

    /**
     * 获取作业ID
     * @return 作业ID
     */
    String getId();

    /**
     * 所需的CPU核心数，小于等于0表示此作业未定义CPU需求。在分发作业时，会根据此方法返回的CPU核心需求数量来检测一个worker是否有能力执行此作业。
     * @return 作业所需的CPU核心数
     */
    float getCpuRequirement();

    /**
     * 所需的内存GB数，小于等于0表示此作业未定义内存需求。在分发作业时，会根据此方法返回的内存需求数量来检测一个worker是否有能力执行此作业。
     * @return 作业执行所需的内存大小，单位GB。
     */
    float getRamRequirement();

    /**
     * 获取作业调度方式。
     * @return 作业调度方式
     */
    JobScheduleType getScheduleType();

    /**
     * 获取作业分发类型。
     * @return 作业分发类型
     */
    JobDispatchType getDispatchType();

    /**
     * 获取作业延迟时间。
     * 当调度方式为{@link JobScheduleType#DELAYED}或{@link JobScheduleType#FIXED_INTERVAL}时，
     * 表示从<code>createdAt</code>时间点开始，延迟多久触发作业调度。
     * @return 作业延迟时间
     */
    Duration getScheduleDelay();

    /**
     * 获取作业调度间隔时间。
     * 当调度方式为{@link JobScheduleType#FIXED_INTERVAL}时，表示前一次作业调度执行完成后，隔多久触发第二次调度。
     * 当调度方式为{@link JobScheduleType#FIXED_RATE}时，表示前一次作业调度下发后，隔多久触发第二次调度。
     * @return 作业调度间隔时间
     */
    Duration getScheduleInterval();

    /**
     * 获取作业调度的CRON表达式。
     * 当调度方式为{@link JobScheduleType#CRON}时，根据此CRON表达式计算得到的时间点触发作业调度。
     * @return 作业调度的CRON表达式
     */
    String getScheduleCron();

    /**
     * 获取作业的创建时间
     * @return 作业创建时间
     */
    LocalDateTime getCreatedAt();

}
