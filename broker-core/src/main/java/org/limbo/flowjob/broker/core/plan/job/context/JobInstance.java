package org.limbo.flowjob.broker.core.plan.job.context;

import lombok.Data;
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.utils.strings.UUIDUtils;

import java.time.Instant;

/**
 * @author Devil
 * @since 2021/9/2
 */
@Data
public class JobInstance {

    /**
     * JobInstance 唯一ID，多字段联合
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


    /**
     * JobInstance下发给Worker时，以TaskInfo的形式下发
     */
    public TaskInfo taskInfo(Job job) {
        TaskInfo task = new TaskInfo();
        task.setPlanId(id.planId);
        task.setPlanRecordId(id.planRecordId);
        task.setPlanInstanceId(id.planInstanceId);
        task.setJobId(id.jobId);
        task.setJobInstanceId(id.jobInstanceId);
        task.setTaskId(UUIDUtils.randomID());
        task.setType(TaskType.NORMAL); // TODO 哪里来？
        task.setAttributes(new Attributes());
        task.setDispatchOption(job.getDispatchOption());
        task.setExecutorOption(job.getExecutorOption());
        return task;
    }


    /**
     * 值对象，JobInstance 多字段联合ID
     */
    @Data
    public static class ID {

        public final Long planId;

        public final Long planRecordId;

        public final Integer planInstanceId;

        public final String jobId;

        public final Integer jobInstanceId;

        public ID(Long planId, Long planRecordId, Integer planInstanceId, String jobId, Integer jobInstanceId) {
            this.planId = planId;
            this.planRecordId = planRecordId;
            this.planInstanceId = planInstanceId;
            this.jobId = jobId;
            this.jobInstanceId = jobInstanceId;
        }
    }

}
