package org.limbo.flowjob.tracker.core.job.context;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.ExecutorOption;
import org.limbo.flowjob.tracker.core.storage.Storable;

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Data
public class JobRecord implements Storable {

    private String planId;

    private Long planRecordId;

    private Long planInstanceId;

    private String jobId;

    /**
     * 作业分发配置参数
     */
    private DispatchOption dispatchOption;

    /**
     * 作业执行器配置参数
     */
    private ExecutorOption executorOption;

    /**
     * 状态
     */
    private JobScheduleStatus state;

    /**
     * 此次执行的参数
     */
    private String attributes;

    /**
     * 已经重试的次数 todo 可以不要这个字段，直接从db获取instance个数   不管用不用这个字段，可能存在worker重复反馈导致数据问题
     */
    private Integer retry;

    /**
     * 开始时间
     */
    private LocalDateTime startAt;

    /**
     * 结束时间
     */
    private LocalDateTime endAt;

    public JobInstance newInstance(Long jobInstanceId, JobScheduleStatus state) {
        JobInstance instance = new JobInstance();
        instance.setPlanId(planId);
        instance.setPlanRecordId(planRecordId);
        instance.setPlanInstanceId(planInstanceId);
        instance.setJobId(jobId);
        instance.setJobInstanceId(jobInstanceId);
        instance.setState(state);
        instance.setStartAt(TimeUtil.nowInstant());
        return instance;
    }

}
