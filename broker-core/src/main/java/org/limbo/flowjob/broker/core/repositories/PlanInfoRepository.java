package org.limbo.flowjob.broker.core.repositories;

import org.limbo.flowjob.broker.core.plan.PlanInfo;

/**
 * @author Brozen
 * @since 2021-10-19
 */
public interface PlanInfoRepository {


    /**
     * 添加新的执行计划版本信息
     * @param planInfo 执行计划版本信息
     */
    void addVersion(PlanInfo planInfo);


    /**
     * 根据计划ID、版本号查询计划详情
     * @param planId 执行计划ID
     * @param version 版本号
     * @return 作业执行计划详情
     */
    PlanInfo getByVersion(String planId, Integer version);

}
