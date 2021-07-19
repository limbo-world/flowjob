package org.limbo.flowjob.tracker.admin.service.plan;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.dto.job.JobDto;
import org.limbo.flowjob.tracker.commons.dto.plan.DispatchOptionDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanAddDto;
import org.limbo.flowjob.tracker.commons.dto.plan.ScheduleOptionDto;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.ScheduleOption;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.core.schedule.DelegatedScheduleCalculator;
import org.limbo.flowjob.tracker.core.schedule.scheduler.HashedWheelTimerScheduler;
import org.limbo.flowjob.tracker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.tracker.dao.po.PlanPO;
import org.limbo.flowjob.tracker.infrastructure.plan.converters.PlanPoConverter;
import org.limbo.flowjob.tracker.infrastructure.plan.repositories.PlanPoRepository;
import org.limbo.utils.verifies.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Devil
 * @date 2021/7/14 5:04 下午
 */
@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PlanPoRepository planPoRepository;

    @Autowired
    private PlanPoConverter planPoConverter;

    @Autowired
    private HashedWheelTimerScheduler<PlanInstance> scheduler;

    /**
     * 新增计划
     * todo 不应该为 add update 方式 因为如果update了plan的调度方式，比如调度间隔等，需要修改时间轮里面的plan的do
     * @param dto
     * @return
     */
    public String add(PlanAddDto dto) {
        return planRepository.addOrUpdatePlan(convertToDo(dto));
    }

    /**
     * 启动计划 开始调度
     * @param planId
     */
    public void enable(String planId) {
        PlanPO planPO = planPoRepository.getById(planId);
        Verifies.notNull(planPO, "计划不存在");

        if (planPO.getIsEnabled()) {
            return;
        }

        planPoRepository.switchEnable(planId, true);

        // 调度 plan
        Plan plan = planPoConverter.reverse().convert(planPO);
        scheduler.schedule(plan);
    }

    /**
     * 取消计划 停止调度
     * @param planId
     */
    public void disable(String planId) {
        PlanPO planPO = planPoRepository.getById(planId);
        Verifies.notNull(planPO, "计划不存在");

        if (!planPO.getIsEnabled()) {
            return;
        }

        planPoRepository.switchEnable(planId, false);

        // 停止调度 plan
        scheduler.unschedule(planId);
    }


    public Plan convertToDo(PlanAddDto dto) {

        ScheduleType scheduleType = dto.getScheduleOption().getScheduleType();
        DelegatedScheduleCalculator delegatedCalculator = new DelegatedScheduleCalculator(scheduleType);

        Plan plan = new Plan(delegatedCalculator);
        plan.setPlanDesc(dto.getPlanDesc());
        plan.setDispatchOption(convertToDo(dto.getDispatchOption()));
        plan.setScheduleOption(convertToDo(dto.getScheduleOption()));
        plan.setJobs(convertToDo(dto.getJobs()));

        return plan;
    }

    public DispatchOption convertToDo(DispatchOptionDto dto) {
        return new DispatchOption(dto.getDispatchType(), dto.getCpuRequirement(), dto.getRamRequirement());
    }

    public ScheduleOption convertToDo(ScheduleOptionDto dto) {
        return new ScheduleOption(dto.getScheduleType(), dto.getScheduleStartAt(), dto.getScheduleDelay(),
                dto.getScheduleInterval(), dto.getScheduleCron());
    }

    public List<Job> convertToDo(List<JobDto> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return null;
        }
        List<Job> list = new ArrayList<>();
        for (JobDto dto : dtos) {
//            list.add(new Job())// todo
        }
        return null;
    }


}
