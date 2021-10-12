package org.limbo.flowjob.tracker.core.plan;

import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface PlanInstanceRepository {

    /**
     * 持久化实例
     * @param instance 实例
     */
    void add(PlanInstance instance);

    /**
     * 实例结束
     */
    void end(String planId, Long planRecordId, Integer planInstanceId, PlanScheduleStatus state);

    /**
     * 获取实例
     * @param planId 计划ID
     * @param planInstanceId 实例ID
     * @return 实例
     */
    PlanInstance get(String planId, Long planRecordId, Integer planInstanceId);

    List<PlanInstance> list(String planId, Long planRecordId);

    /**
     * 创建ID
     */
    Integer createId(String planId, Long planRecordId);
}
