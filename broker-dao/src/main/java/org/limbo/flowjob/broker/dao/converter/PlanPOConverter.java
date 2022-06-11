package org.limbo.flowjob.broker.dao.converter;

import com.google.common.base.Converter;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Brozen
 * @since 2021-10-19
 */
@Component
public class PlanPOConverter extends Converter<Plan, PlanEntity> {

    @Autowired
    private ApplicationContext ac;

    /**
     * {@link Plan} -> {@link PlanEntity}
     */
    @Override
    protected PlanEntity doForward(Plan plan) {
        PlanEntity po = new PlanEntity();
        po.setPlanId(plan.getPlanId());
        po.setCurrentVersion(plan.getCurrentVersion());
        po.setRecentlyVersion(plan.getRecentlyVersion());
        po.setIsEnabled(plan.isEnabled());
        return po;
    }


    /**
     * {@link PlanEntity} -> {@link Plan}
     */
    @Override
    protected Plan doBackward(PlanEntity po) {
        Plan plan = new Plan();
        plan.setPlanId(po.getPlanId());
        plan.setCurrentVersion(po.getCurrentVersion());
        plan.setRecentlyVersion(po.getRecentlyVersion());
        plan.setEnabled(po.getIsEnabled());

        // 注入依赖
        ac.getAutowireCapableBeanFactory().autowireBean(plan);

        return plan;
    }


}
