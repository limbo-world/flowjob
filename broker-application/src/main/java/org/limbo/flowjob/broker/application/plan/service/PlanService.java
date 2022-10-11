package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import org.limbo.flowjob.broker.api.console.param.PlanAddParam;
import org.limbo.flowjob.broker.api.console.param.PlanReplaceParam;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.application.plan.converter.PlanConverter;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanInfo;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanRepository;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.utils.Verifies;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * @author Brozen
 * @since 2022-06-11
 */
@Service
public class PlanService {

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;

    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepository;


    /**
     * 新增执行计划
     *
     * @param param 新增计划参数
     * @return planId
     */
    @Transactional
    public String add(PlanAddParam param) {
        Plan plan = PlanConverter.convertPlan(param);
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

        PlanInfo planInfo = PlanConverter.convertPlanInfo(param);
        plan.setInfo(planInfo);

        return planRepository.save(plan);
    }


    /**
     * 启动计划，开始调度
     *
     * @param planId 计划ID
     */
    @Transactional
    public boolean start(String planId) {
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(Long.valueOf(planId));
        Verifies.verify(planEntityOptional.isPresent(), String.format("Cannot find Plan %s", planId));

        PlanEntity planEntity = planEntityOptional.get();
        // 已经启动不重复处理
        if (Boolean.TRUE.equals(planEntity.getIsEnabled())) {
            return true;
        }

        return planEntityRepo.updateEnable(planEntity.getId(), false, true) == 1;
    }

    /**
     * 取消计划 停止调度
     */
    @Transactional
    public boolean stop(String planId) {
        // 获取当前的plan数据
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(Long.valueOf(planId));
        Verifies.verify(planEntityOptional.isPresent(), String.format("Cannot find Plan %s", planId));

        // 已经停止不重复处理
        PlanEntity planEntity = planEntityOptional.get();
        // 已经停止不重复处理
        if (Boolean.FALSE.equals(planEntity.getIsEnabled())) {
            return true;
        }

        // 停用计划
        return planEntityRepo.updateEnable(planEntity.getId(), true, false) == 1;
    }

    @Transactional
    public void saveScheduleInfo(PlanInstance planInstance, List<JobInstance> jobInstances) {
        Long planId = Long.valueOf(planInstance.getPlanId());

        // 加锁
        planEntityRepo.selectForUpdate(planId);

        // 判断并发情况下 是否已经有人提交调度任务 如有则无需处理
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo
                .findByPlanIdAndExpectTriggerAtAndTriggerType(
                        planId, planInstance.getExpectTriggerAt(), TriggerType.SCHEDULE.type
                );
        if (planInstanceEntity != null) {
            return;
        }

        planInstanceRepository.save(planInstance);

        // 批量保存数据
        jobInstanceRepository.saveAll(jobInstances);

        // 更新plan的下次触发时间
        if (ScheduleType.FIXED_DELAY != planInstance.getScheduleOption().getScheduleType() && TriggerType.SCHEDULE == planInstance.getScheduleOption().getTriggerType()) {
            planEntityRepo.nextTriggerAt(planId, planInstance.nextTriggerAt());
        }
    }
}
