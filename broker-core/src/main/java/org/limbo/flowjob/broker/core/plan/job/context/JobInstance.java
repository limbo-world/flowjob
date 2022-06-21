package org.limbo.flowjob.broker.core.plan.job.context;

import lombok.Data;
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.handler.JobFailHandler;

import java.io.Serializable;
import java.time.Instant;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Data
public class JobInstance implements Serializable {

    private static final long serialVersionUID = -4343833583716806197L;

    private String jobInstanceId;

    private String planId;

    private String planInstanceId;

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

    /**
     * JobInstance下发给Worker时，以TaskInfo的形式下发
     */
    public TaskInfo newTask(Job job) {
        TaskInfo task = new TaskInfo();
        task.setPlanId(planId);
        task.setPlanInstanceId(planInstanceId);
        task.setJobId(jobId);
        task.setJobInstanceId(jobInstanceId);
        task.setType(TaskType.NORMAL); // TODO 哪里来？
        task.setAttributes(new Attributes());
        task.setDispatchOption(job.getDispatchOption());
        task.setExecutorOption(job.getExecutorOption());
        return task;
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
