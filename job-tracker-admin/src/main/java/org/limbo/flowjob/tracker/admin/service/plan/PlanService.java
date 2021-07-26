package org.limbo.flowjob.tracker.admin.service.plan;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.tracker.commons.dto.job.JobDto;
import org.limbo.flowjob.tracker.commons.dto.plan.DispatchOptionDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanAddDto;
import org.limbo.flowjob.tracker.commons.dto.plan.ScheduleOptionDto;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.ScheduleOption;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanFactory;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.tracker.dao.po.PlanPO;
import org.limbo.flowjob.tracker.infrastructure.plan.repositories.PlanPoRepository;
import org.limbo.utils.UUIDUtils;
import org.limbo.utils.verifies.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PlanPoRepository planPoRepository;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JobInstanceRepository jobInstanceRepository;

    @Autowired
    private PlanFactory planFactory;

    /**
     * 新增计划 只是个落库操作
     *
     * @param dto
     * @return
     */
    public String add(PlanAddDto dto) {
        // 保存 plan
        Plan plan = convertToDo(dto);
        return planRepository.addPlan(plan);
    }

    /**
     * 修改计划 可能会触发 内存时间轮改动
     */
    public void update(String planId, String planDesc, ScheduleOptionDto scheduleOption, List<JobDto> jobs) {
        // 获取当前的plan数据
        Plan currentPlan = planRepository.getCurrentPlan(planId);

        if (planDesc != null) {
            currentPlan.setPlanDesc(planDesc);
        }

        // 修改调度信息
        if (scheduleOption != null) {
            currentPlan.setScheduleOption(new ScheduleOption(
                    scheduleOption.getScheduleType() != null ? scheduleOption.getScheduleType() : currentPlan.getScheduleOption().getScheduleType(),
                    scheduleOption.getScheduleStartAt() != null ? scheduleOption.getScheduleStartAt() : currentPlan.getScheduleOption().getScheduleStartAt(),
                    scheduleOption.getScheduleDelay() != null ? scheduleOption.getScheduleDelay() : currentPlan.getScheduleOption().getScheduleDelay(),
                    scheduleOption.getScheduleInterval() != null ? scheduleOption.getScheduleInterval() : currentPlan.getScheduleOption().getScheduleInterval(),
                    scheduleOption.getScheduleCron() != null ? scheduleOption.getScheduleCron() : currentPlan.getScheduleOption().getScheduleCron()
            ));
        }

        // 修改job信息
        currentPlan.setJobs(convertToDo(jobs));

        Integer newVersion = planRepository.newPlanVersion(currentPlan);
        currentPlan.setVersion(newVersion);

        // 需要修改plan重新调度
        if (scheduler.isScheduling(planId)) {
            scheduler.unschedule(planId);
            scheduler.schedule(currentPlan);
        }
    }

    /**
     * 启动计划 开始调度 todo 并发
     *
     * @param planId
     */
    public void enable(String planId) {
        PlanPO planPO = planPoRepository.getById(planId);
        Verifies.notNull(planPO, "plan is not exist");

        if (planPO.getIsEnabled() || planPO.getIsDeleted()) {
            return;
        }

        planPoRepository.switchEnable(planId, true);

        // 调度 plan
        scheduler.schedule(planRepository.getPlan(planId, planPO.getCurrentVersion()));
    }

    /**
     * 取消计划 停止调度
     *
     * @param planId
     */
    public void disable(String planId) {
        PlanPO planPO = planPoRepository.getById(planId);
        Verifies.notNull(planPO, "plan is not exist");

        if (planPO.getIsEnabled() || planPO.getIsDeleted()) {
            return;
        }

        planPoRepository.switchEnable(planId, false);

        // 停止调度 plan
        scheduler.unschedule(planId);
    }


    public Plan convertToDo(PlanAddDto dto) {
        return planFactory.create(StringUtils.isBlank(dto.getPlanId()) ? UUIDUtils.randomID() : dto.getPlanId(),
                0,
                dto.getPlanDesc(),
                convertToDo(dto.getScheduleOption()),
                convertToDo(dto.getJobs()));
    }

    public DispatchOption convertToDo(DispatchOptionDto dto) {
        if (dto == null) {
            return null;
        }
        return new DispatchOption(dto.getDispatchType(), dto.getCpuRequirement(), dto.getRamRequirement());
    }

    public ScheduleOption convertToDo(ScheduleOptionDto dto) {
        if (dto == null) {
            return null;
        }
        return new ScheduleOption(dto.getScheduleType(), dto.getScheduleStartAt(), dto.getScheduleDelay(),
                dto.getScheduleInterval(), dto.getScheduleCron());
    }

    public List<Job> convertToDo(List<JobDto> dtos) {
        List<Job> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(dtos)) {
            return list;
        }
        for (JobDto dto : dtos) {
            list.add(convertToDo(dto));
        }
        // todo 检测是否成环
        return list;
    }

    public Job convertToDo(JobDto dto) {
        Job job = new Job();
        job.setJobId(dto.getJobId());
        job.setJobDesc(dto.getJobDesc());
        job.setParentJobIds(dto.getParentJobIds());
        job.setDispatchOption(convertToDo(dto.getDispatchOption()));
        return job;
    }


}
