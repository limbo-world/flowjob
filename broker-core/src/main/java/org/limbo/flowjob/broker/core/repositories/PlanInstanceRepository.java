package org.limbo.flowjob.broker.core.repositories;

import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.plan.PlanInstance;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface PlanInstanceRepository {

    /**
     * 持久化
     */
    void add(PlanInstance record);


    /**
     * 获取
     */
    PlanInstance get(PlanInstance.ID planRecordId);


    /**
     * 创建ID
     */
    PlanInstance.ID createId(String planId);


    void end(PlanInstance.ID planRecordId, PlanScheduleStatus state);

}
