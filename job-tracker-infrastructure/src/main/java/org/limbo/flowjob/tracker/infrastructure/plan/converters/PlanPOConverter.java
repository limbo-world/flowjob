package org.limbo.flowjob.tracker.infrastructure.plan.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.dao.po.PlanPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Brozen
 * @since 2021-10-19
 */
@Component
public class PlanPOConverter extends Converter<Plan, PlanPO> {

    @Autowired
    private ApplicationContext ac;

    /**
     * {@link Plan} -> {@link PlanPO}
     */
    @Override
    protected PlanPO doForward(Plan plan) {
        PlanPO po = new PlanPO();
        po.setPlanId(plan.getPlanId());
        po.setCurrentVersion(plan.getCurrentVersion());
        po.setRecentlyVersion(plan.getRecentlyVersion());
        po.setIsEnabled(plan.isEnabled());
        return po;
    }


    /**
     * {@link PlanPO} -> {@link Plan}
     */
    @Override
    protected Plan doBackward(PlanPO po) {
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
