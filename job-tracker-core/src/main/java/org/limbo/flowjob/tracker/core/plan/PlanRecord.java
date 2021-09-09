package org.limbo.flowjob.tracker.core.plan;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.job.JobDAG;
import org.limbo.flowjob.tracker.core.storage.Storable;

import java.io.Serializable;
import java.time.Instant;

/**
 * @author Devil
 * @since 2021/9/1
 */
@Data
public class PlanRecord implements Storable, Serializable {

    private static final long serialVersionUID = 1837382860200548371L;
    /**
     * 计划ID
     */
    private String planId;

    private Long planRecordId;

    /**
     * 计划的版本
     */
    private Integer version;

    private PlanScheduleStatus state;

    /**
     * 已经重试的次数
     */
    private Integer retry;

    /**
     * 是否手动下发
     */
    private boolean manual;

    /**
     * 开始时间
     */
    private Instant startAt;

    /**
     * 结束时间
     */
    private Instant endAt;

    // ===== 非 po 属性

    private JobDAG dag;

    public PlanInstance newInstance(Long planInstanceId, PlanScheduleStatus state) {
        PlanInstance instance = new PlanInstance();
        instance.setPlanId(planId);
        instance.setPlanRecordId(planRecordId);
        instance.setPlanInstanceId(planInstanceId);
        instance.setState(state);
        instance.setStartAt(TimeUtil.nowInstant());
        return instance;
    }

}
