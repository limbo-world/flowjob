package org.limbo.flowjob.tracker.core.plan;

import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;

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
    PlanRecord get(PlanRecord.ID planRecordId);


    /**
     * 创建ID
     */
    PlanRecord.ID createId(String planId);


    void end(PlanRecord.ID planRecordId, PlanScheduleStatus state);

}
