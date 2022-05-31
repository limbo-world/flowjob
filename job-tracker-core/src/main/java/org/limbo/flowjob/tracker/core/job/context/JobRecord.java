package org.limbo.flowjob.tracker.core.job.context;

import lombok.Data;
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.job.handler.JobFailHandler;

import java.io.Serializable;
import java.time.Instant;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Data
public class JobRecord implements Serializable {

    private static final long serialVersionUID = -4343833583716806197L;

    /**
     * JobRecord 多字段联合ID
     */
    private ID id;

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

    public JobInstance newInstance(JobInstance.ID jobInstanceId, JobScheduleStatus state) {
        JobInstance instance = new JobInstance();
        instance.setId(jobInstanceId);
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


    /**
     * 值对象，JobRecord 多字段联合ID
     */
    @Data
    public static class ID {

        public final String planId;

        public final Long planRecordId;

        public final Integer planInstanceId;

        public final String jobId;

        public ID(String planId, Long planRecordId, Integer planInstanceId, String jobId) {
            this.planId = planId;
            this.planRecordId = planRecordId;
            this.planInstanceId = planInstanceId;
            this.jobId = jobId;
        }
    }

}
