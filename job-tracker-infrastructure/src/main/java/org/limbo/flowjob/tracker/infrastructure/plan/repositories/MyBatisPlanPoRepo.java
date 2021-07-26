package org.limbo.flowjob.tracker.infrastructure.plan.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.dao.mybatis.PlanMapper;
import org.limbo.flowjob.tracker.dao.po.PlanPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Repository
public class MyBatisPlanPoRepo implements PlanPoRepository {

    @Autowired
    private PlanMapper planMapper;

    @Override
    public PlanPO getById(String planId) {
        return planMapper.selectById(planId);
    }

    @Override
    public void switchEnable(String planId, boolean isEnabled) {
        planMapper.update(null, Wrappers.<PlanPO>lambdaUpdate()
                .set(PlanPO::getIsEnabled, isEnabled)
                .eq(PlanPO::getPlanId, planId)
        );
    }

}
