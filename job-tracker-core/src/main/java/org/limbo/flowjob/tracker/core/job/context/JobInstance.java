package org.limbo.flowjob.tracker.core.job.context;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;

import java.time.Instant;

/**
 * @author Devil
 * @since 2021/9/2
 */
@Data
public class JobInstance {

    private String planId;

    private Long planRecordId;

    private Integer planInstanceId;

    private String jobId;

    private Integer jobInstanceId;

    /**
     * 状态
     */
    private JobScheduleStatus state;

    /**
     * 开始时间
     */
    private Instant startAt;

    /**
     * 结束时间
     */
    private Instant endAt;

    public TaskInfo taskInfo() {
        return null;
    }

}
