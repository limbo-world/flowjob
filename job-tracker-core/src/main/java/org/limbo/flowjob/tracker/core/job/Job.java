package org.limbo.flowjob.tracker.core.job;

import lombok.Data;

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
    private String jobId;

    /**
     * 作业描述
     */
    private String jobDesc;

    /**
     * 作业分发配置参数
     */
    private JobDispatchOption dispatchOption;

    /**
     * 作业调度配置参数
     */
    private JobScheduleOption scheduleOption;

    /**
     * 作业创建时间
     */
    private LocalDateTime createdAt;

}
