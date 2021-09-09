package org.limbo.flowjob.tracker.core.job.context;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.ExecutorOption;
import org.limbo.flowjob.tracker.core.job.handler.JobFailHandler;
import org.limbo.flowjob.tracker.core.storage.Storable;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Data
public class JobRecord implements Storable, Serializable {

    private static final long serialVersionUID = -4343833583716806197L;

    private String planId;

    private Long planRecordId;

    private Long planInstanceId;

    private String jobId;

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

    // ===== 非 po 属性

    /**
     * 已经重试的次数 todo 可以不要这个字段，直接从db获取instance个数   不管用不用这个字段，可能存在worker重复反馈导致数据问题
     */
    private Integer retry;

    /**
     * 此次执行的参数
     */
    private String attributes;

    /**
     * 失败时候的处理
     */
    private JobFailHandler failHandler;

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

    /**
     * 是否能触发下级任务
     */
    public boolean canTriggerNext() {
        if (JobScheduleStatus.SUCCEED == state) {
            return true;
        } else if (JobScheduleStatus.FAILED == state) {
            // todo 根据 handler 类型来判断
            return true;
        } else {
            return false;
        }
    }

}
