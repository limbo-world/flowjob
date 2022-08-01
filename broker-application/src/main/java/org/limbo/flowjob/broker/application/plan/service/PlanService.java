package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import org.limbo.flowjob.broker.api.console.param.PlanAddParam;
import org.limbo.flowjob.broker.api.console.param.PlanReplaceParam;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.application.plan.converter.PlanConverter;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanRepository;
import org.limbo.flowjob.common.utils.Verifies;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * @author Brozen
 * @since 2022-06-11
 */
@Component
public class PlanService {

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    @Setter(onMethod_ = @Inject)
    private PlanConverter converter;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;

    /**
     * 新增执行计划
     * @param param 新增计划参数
     * @return planId
     */
    @Transactional
    public String addPlan(PlanAddParam param) {
        Plan plan = converter.convertPlan(param);
        return planRepository.save(plan);
    }


    /**
     * 覆盖计划 可能会触发 内存时间轮改动
     */
    @Transactional
    public String replace(String planId, PlanReplaceParam param) {
        // 获取当前的plan数据
        Plan plan = planRepository.get(planId);
        Verifies.notNull(plan, String.format("Cannot find Plan %s", planId));

        PlanInfo planInfo = converter.convertPlanInfo(param);
        return plan.newVersion(planInfo);
    }


    /**
     * 启动计划，开始调度
     * @param planId 计划ID
     */
    @Transactional
    public boolean start(String planId) {
        Plan plan = planRepository.get(planId);
        Verifies.notNull(plan, String.format("Cannot find Plan %s", planId));

        // 已经停止不重复处理
        if (plan.isEnabled()) {
            return true;
        }

        return plan.enable();
    }


    /**
     * 取消计划 停止调度
     */
    @Transactional
    public boolean stop(String planId) {
        // 获取当前的plan数据
        Plan plan = planRepository.get(planId);
        Verifies.notNull(plan, String.format("Cannot find Plan %s", planId));

        // 已经停止不重复处理
        if (!plan.isEnabled()) {
            return true;
        }

        // 停用计划
        return plan.disable();
    }

    // todo 交由调度器执行
    @Transactional
    public void schedule(PlanInstance planInstance) {
        // 持久化数据
        planInstanceRepository.savePlanInstanceScheduleInfo(planInstance);
        // 更新plan的下次触发时间
        if (ScheduleType.FIXED_DELAY != planInstance.getScheduleOption().getScheduleType() && TriggerType.SCHEDULE == planInstance.getScheduleOption().getTriggerType()) {
            planRepository.nextTriggerAt(planInstance.getPlanId(), planInstance.nextTriggerAt());
        }
        // todo 上面两个在事务中 先执行提交事务
        planInstance.schedule();
        // todo 下发后需要将任务下发情况更新
        // if (dispatched) {
        //      taskRepo.dispatched(task);
        //            } else {
        //                taskRepo.dispatchFailed(task);
        //            }

        // jobInstanceRepo.dispatched(this);

        // jobInstanceRepo.dispatchFailed(this);
        // todo 判断是否有重试次数 放入重试队列
    }

}
