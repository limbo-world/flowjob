package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import org.limbo.flowjob.broker.api.console.param.PlanAddParam;
import org.limbo.flowjob.broker.api.console.param.PlanReplaceParam;
import org.limbo.flowjob.broker.api.constants.enums.PlanStatus;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.application.plan.converter.PlanConverter;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanRepository;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.utils.TimeUtil;
import org.limbo.flowjob.common.utils.Verifies;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

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
    private TaskRepository taskRepository;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepository;

    /**
     * 新增执行计划
     *
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
        plan.setInfo(planInfo);

        return planRepository.updateVersion(plan);
    }


    /**
     * 启动计划，开始调度
     *
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

        return planRepository.enablePlan(plan);
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
        return planRepository.disablePlan(plan);
    }

    // todo 批量保存 没有重复利用价值就放到linstener
    @Transactional
    public void saveScheduleInfo(PlanInstance planInstance) {
        // planInstance 状态
        planInstanceEntityRepo.start(
                Long.valueOf(planInstance.getPlanInstanceId()),
                PlanStatus.SCHEDULING.status,
                PlanStatus.EXECUTING.status,
                TimeUtil.currentLocalDateTime()
        );
        // 批量保存数据
        List<JobInstance> jobInstances = planInstance.scheduleJobInstances();
        for (JobInstance jobInstance : jobInstances) {
            jobInstanceRepository.add(jobInstance);
            List<Task> tasks = jobInstance.createTasks();
            for (Task task : tasks) {
                taskRepository.save(task);
            }
        }

        // 更新plan的下次触发时间
        if (ScheduleType.FIXED_DELAY != planInstance.getScheduleOption().getScheduleType() && TriggerType.SCHEDULE == planInstance.getScheduleOption().getTriggerType()) {
            planRepository.nextTriggerAt(planInstance.getPlanId(), planInstance.nextTriggerAt());
        }
    }

}
