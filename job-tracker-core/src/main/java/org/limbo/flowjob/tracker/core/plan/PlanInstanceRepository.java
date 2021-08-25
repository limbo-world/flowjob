package org.limbo.flowjob.tracker.core.plan;

import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface PlanInstanceRepository {

    /**
     * 持久化实例
     * @param instance 实例
     */
    default void addInstance(PlanInstance instance) {
        throw new UnsupportedOperationException();
    }

    /**
     * 实例结束
     */
    default void endInstance(String planId, Long planInstanceId, PlanScheduleStatus state) {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取实例
     * @param planId 计划ID
     * @param planInstanceId 实例ID
     * @return 实例
     */
    default PlanInstance getInstance(String planId, Long planInstanceId) {
        throw new UnsupportedOperationException();
    }

    /**
     * 创建ID
     */
    Long createId(String planId);
}
