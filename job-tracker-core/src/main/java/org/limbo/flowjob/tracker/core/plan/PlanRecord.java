package org.limbo.flowjob.tracker.core.plan;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.job.JobDAG;
import org.limbo.flowjob.tracker.core.storage.Storable;

import java.time.Instant;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Data
public class PlanRecord implements Storable {

    /**
     * 计划ID
     */
    private String planId;

    private Long planRecordId;

    /**
     * 计划的版本
     */
    private Integer version;

    private JobDAG dag;

    private PlanScheduleStatus state;

    /**
     * 已经重试的次数
     */
    private Integer retry;

    /**
     * 是否需要重新调度 目前只有 FIXED_INTERVAL 类型在任务执行完成后才会需要重新调度
     */
    private boolean reschedule;

    /**
     * 开始时间
     */
    private Instant startAt;

    /**
     * 结束时间
     */
    private Instant endAt;

    public PlanInstance newInstance(Long planInstanceId, PlanScheduleStatus state) {
        PlanInstance instance = new PlanInstance();
        instance.setPlanId(planId);
        instance.setPlanRecordId(planRecordId);
        instance.setPlanInstanceId(planInstanceId);
        instance.setDag(dag);
        instance.setState(state);
        instance.setStartAt(TimeUtil.nowInstant());
        return instance;
    }
}
