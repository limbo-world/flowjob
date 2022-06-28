package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import org.limbo.flowjob.broker.api.console.param.PlanAddParam;
import org.limbo.flowjob.broker.api.console.param.PlanReplaceParam;
import org.limbo.flowjob.broker.application.plan.converter.PlanConverter;
import org.limbo.flowjob.broker.core.broker.TrackerNode;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.PlanScheduler;
import org.limbo.flowjob.broker.core.repositories.PlanRepository;
import org.limbo.flowjob.broker.core.repositories.PlanSchedulerRepository;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Brozen
 * @since 2022-06-11
 */
@Component
public class PlanService {

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepo;

    @Setter(onMethod_ = @Inject)
    private PlanSchedulerRepository planSchedulerRepo;

    @Setter(onMethod_ = @Inject)
    private TrackerNode trackerNode;

    @Setter(onMethod_ = @Inject)
    private PlanConverter converter;


    /**
     * 新增执行计划
     * @param param 新增计划参数
     * @return planId
     */
    public String addPlan(PlanAddParam param) {
        Plan plan = converter.convertPlan(param);
        return planRepo.save(plan);
    }


    /**
     * 覆盖计划 可能会触发 内存时间轮改动
     */
    @Transactional
    public void replace(String planId, PlanReplaceParam param) {
        // 获取当前的plan数据
        Plan plan = planRepo.get(planId);
        Verifies.notNull(plan, String.format("Cannot find Plan %s", planId));

        // TODO plan 创建新版本 并切换内存中的信息
        PlanInfo planInfo = converter.convertPlanInfo(param);
        String newVersion = plan.addNewVersion(planInfo);

        // 需要修改plan重新调度
        PlanScheduler scheduler = planSchedulerRepo.get(newVersion);
        if (trackerNode.jobTracker().isScheduling(planId)) {
            trackerNode.jobTracker().unschedule(planId);
            trackerNode.jobTracker().schedule(scheduler);
        }
    }


    /**
     * 启动计划，开始调度
     * @param planId 计划ID
     */
    @Transactional
    public void start(String planId) {
        Plan plan = planRepo.get(planId);
        Verifies.notNull(plan, String.format("Cannot find Plan %s", planId));
        Verifies.verify(!plan.isEnabled(), String.format("Plan %s already started", planId));

        // 更新作业状态，更新成功后启动调度
        if (plan.enable()) {
            PlanScheduler scheduler = planSchedulerRepo.get(plan.getCurrentVersion());
            trackerNode.jobTracker().schedule(scheduler);
        }
    }


    /**
     * 取消计划 停止调度
     */
    @Transactional
    public void stop(String planId) {
        // 获取当前的plan数据
        Plan plan = planRepo.get(planId);
        Verifies.notNull(plan, String.format("Cannot find Plan %s", planId));

        // 已经停止不重复处理
        if (!plan.isEnabled()) {
            return;
        }

        // 停用计划
        if (plan.disable()) {
            // 停止调度 plan
            trackerNode.jobTracker().unschedule(planId);
        }
    }


}
