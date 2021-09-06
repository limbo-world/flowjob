package org.limbo.flowjob.tracker.core.job.context;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;

import java.time.Instant;
import java.util.List;

/**
 * @author Devil
 * @since 2021/9/2
 */
@Data
public class JobInstance {

    private String planId;

    private Long planRecordId;

    private Long planInstanceId;

    private String jobId;

    private Long jobInstanceId;

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

    public List<Task> tasks() {
        // 根据下发类型 单机 广播 分片
        return null;
    }

}
