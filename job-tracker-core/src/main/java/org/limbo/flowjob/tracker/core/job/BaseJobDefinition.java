package org.limbo.flowjob.tracker.core.job;

import lombok.Getter;
import org.limbo.flowjob.tracker.core.executor.dispatcher.JobDispatchType;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.JobScheduleType;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2021-05-27
 */
public class BaseJobDefinition {

    /**
     * 作业ID
     */
    @Getter
    private String id;

    /**
     * CPU内核需求数量
     */
    @Getter
    private float cpuRequirement;

    /**
     * 内存需求数量
     */
    @Getter
    private float ramRequirement;

    /**
     * 作业调度方式
     */
    @Getter
    private JobScheduleType scheduleType;

    /**
     * 作业分发方式
     */
    @Getter
    private JobDispatchType dispatchType;

    /**
     * 作业延迟时间
     * @see Job#getScheduleDelay()
     */
    @Getter
    private Duration scheduleDelay;

    /**
     * 作业调度间隔时间
     * @see Job#getScheduleInterval()
     */
    @Getter
    private Duration scheduleInterval;

    /**
     * 作业调度的CRON表达式
     */
    @Getter
    private String scheduleCron;

    /**
     * 作业创建时间
     */
    @Getter
    private LocalDateTime createdAt;


    public BaseJobDefinition(String id, float cpuRequirement, float ramRequirement,
                     JobScheduleType scheduleType, JobDispatchType dispatchType,
                     Duration scheduleDelay, Duration scheduleInterval,
                     String scheduleCron, LocalDateTime createdAt) {
        this.id = id;
        this.cpuRequirement = cpuRequirement;
        this.ramRequirement = ramRequirement;
        this.scheduleType = scheduleType;
        this.dispatchType = dispatchType;

        this.scheduleDelay = scheduleDelay;
        this.scheduleInterval = scheduleInterval;
        this.scheduleCron = scheduleCron;
        this.createdAt = createdAt;
    }
}
