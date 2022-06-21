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
    String add(PlanInstance instance);


    /**
     * 获取
     */
    PlanInstance get(String planInstanceId);

    /**
     * 结束任务
     */
    void end(String planInstanceId, PlanScheduleStatus state);

}
