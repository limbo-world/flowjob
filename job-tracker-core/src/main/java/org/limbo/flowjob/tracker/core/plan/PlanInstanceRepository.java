package org.limbo.flowjob.tracker.core.plan;

/**
 * @author Devil
 * @date 2021/7/19 5:00 下午
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
     * 更新实例
     * @param instance 实例
     */
    default void updateInstance(PlanInstance instance) {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取实例
     * @param planId 计划ID
     * @param planInstanceId 实例ID
     * @return 实例
     */
    default PlanInstance getInstance(String planId, String planInstanceId) {
        throw new UnsupportedOperationException();
    }
}
