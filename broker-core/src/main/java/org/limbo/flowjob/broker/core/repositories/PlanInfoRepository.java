package org.limbo.flowjob.broker.core.repositories;

import org.limbo.flowjob.broker.core.plan.PlanInfo;

/**
 * @author Brozen
 * @since 2021-10-19
 */
public interface PlanInfoRepository {


    /**
     * 根据计划ID、版本号查询计划详情
     * @return 作业执行计划详情
     */
    PlanInfo getByVersion(Long planInfoId);

}
