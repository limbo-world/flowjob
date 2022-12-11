package org.limbo.flowjob.broker.core.repository;

import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface PlanInstanceRepository {

    /**
     * 持久化
     */
    String save(PlanInstance instance);


    /**
     * 获取
     */
    PlanInstance get(String planInstanceId);

}
