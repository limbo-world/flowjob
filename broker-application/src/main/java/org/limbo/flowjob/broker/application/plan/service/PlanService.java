package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import org.limbo.flowjob.api.param.PlanAddParam;
import org.limbo.flowjob.api.param.PlanReplaceParam;
import org.limbo.flowjob.broker.application.plan.converter.PlanConverter;
import org.limbo.flowjob.broker.core.domain.job.JobFactory;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanFactory;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanRepository;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
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

    @Setter(onMethod_ = @Inject)
    private JobFactory jobFactory;

    @Setter(onMethod_ = @Inject)
    private PlanFactory planFactory;


    /**
     * 新增执行计划
     *
     * @param param 新增计划参数
     * @return planId
     */
    @Transactional
    public String add(PlanAddParam param) {
        Plan plan = planFactory.create(
                TimeUtils.currentLocalDateTime(),
                param.getDescription(),
                PlanConverter.convertScheduleOption(param.getScheduleOption()),
                PlanConverter.convertJob(param.getJobs()),
                false
        );
        return planRepository.save(plan);
    }

    public Plan get(String id) {
        return planRepository.get(id);
    }


    /**
     * 覆盖计划 可能会触发 内存时间轮改动
     */
    @Transactional
    public String replace(String planId, PlanReplaceParam param) {
        // 获取当前的plan数据
        Plan plan = planRepository.get(planId);
        Verifies.notNull(plan, String.format("Cannot find Plan %s", planId));

        Plan newPlan = planFactory.newVersion(plan,
                param.getDescription(),
                PlanConverter.convertScheduleOption(param.getScheduleOption()),
                PlanConverter.convertJob(param.getJobs())
        );
        return planRepository.save(newPlan);
    }


    /**
     * 启动计划，开始调度
     *
     * @param planId 计划ID
     */
    @Transactional
    public boolean start(String planId) {
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(planId);
        Verifies.verify(planEntityOptional.isPresent(), String.format("Cannot find Plan %s", planId));

        PlanEntity planEntity = planEntityOptional.get();
        // 已经启动不重复处理
        if (planEntity.isEnabled()) {
            return true;
        }

        return planEntityRepo.updateEnable(planEntity.getPlanId(), false, true) == 1;
    }

    /**
     * 取消计划 停止调度
     */
    @Transactional
    public boolean stop(String planId) {
        // 获取当前的plan数据
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(planId);
        Verifies.verify(planEntityOptional.isPresent(), String.format("Cannot find Plan %s", planId));

        // 已经停止不重复处理
        PlanEntity planEntity = planEntityOptional.get();
        // 已经停止不重复处理
        if (planEntity.isEnabled()) {
            return true;
        }

        // 停用计划
        return planEntityRepo.updateEnable(planEntity.getPlanId(), true, false) == 1;
    }

    @Transactional
    public void saveScheduleInfo(PlanInstance planInstance, List<JobInstance> rootJobs) {
        String planId = planInstance.getPlanId();

        // 加锁
        planEntityRepo.selectForUpdate(planId);

        // 判断并发情况下 是否已经有人提交调度任务 如有则无需处理 防止重复创建数据
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo
                .findByPlanIdAndExpectTriggerAtAndTriggerType(
                        planId, planInstance.getExpectTriggerAt(), TriggerType.SCHEDULE.type
                );
        if (planInstanceEntity != null) {
            return;
        }

        // 保存 planInstance
        planInstanceRepository.save(planInstance);

        // 保存 jobInstance
        jobInstanceRepository.saveAll(rootJobs);

        // 更新plan的下次触发时间
        if (ScheduleType.FIXED_DELAY != planInstance.getScheduleOption().getScheduleType() && TriggerType.SCHEDULE == planInstance.getScheduleOption().getTriggerType()) {
            planEntityRepo.nextTriggerAt(planId, planInstance.nextTriggerAt());
        }
    }

    public List<JobInstance> rootJobs(PlanInstance planInstance) {
        List<JobInstance> rootJobs = new ArrayList<>();

        for (JobInfo jobInfo : planInstance.getDag().origins()) {
            if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                rootJobs.add(jobFactory.newInstance(planInstance, jobInfo, TimeUtils.currentLocalDateTime()));
            }
        }
        return rootJobs;
    }

}
