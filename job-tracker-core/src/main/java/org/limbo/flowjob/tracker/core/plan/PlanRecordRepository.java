package org.limbo.flowjob.tracker.core.plan;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface PlanRecordRepository {

    /**
     * 持久化
     */
    void add(PlanRecord record);

    /**
     * 获取
     */
    PlanRecord get(String planId, Long planRecordId);

    /**
     * 创建ID
     */
    Long createId(String planId);
}
