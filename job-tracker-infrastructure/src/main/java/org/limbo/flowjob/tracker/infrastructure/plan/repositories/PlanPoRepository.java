package org.limbo.flowjob.tracker.infrastructure.plan.repositories;

import org.limbo.flowjob.tracker.dao.po.PlanPO;

/**
 * @author Devil
 * @date 2021/7/15 10:49 上午
 */
public interface PlanPoRepository {

    PlanPO getById(String planId);

    void switchEnable(String planId, boolean isEnabled);

}
