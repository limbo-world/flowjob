package org.limbo.flowjob.broker.application.plan.service;

import org.limbo.flowjob.broker.api.param.plan.PlanAddParam;
import org.limbo.flowjob.broker.application.plan.converter.PlanConverter;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.repositories.PlanRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Brozen
 * @since 2022-06-11
 */
@ApplicationScoped
public class PlanService {


    private final PlanConverter converter = PlanConverter.INSTANCE;

    @Inject
    private PlanRepository planRepo;


    /**
     * 新增执行计划
     * @param param 新增计划参数
     * @return planId
     */
    public String addPlan(PlanAddParam param) {
        Plan plan = converter.convertPlan(param);
        return planRepo.addPlan(plan);
    }


}
