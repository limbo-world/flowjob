package org.limbo.flowjob.tracker.infrastructure.plan.repositories;

import org.limbo.flowjob.tracker.dao.po.PlanPO;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface PlanPoRepository {

    PlanPO getById(String planId);

    void switchEnable(String planId, boolean isEnabled);

}
