package org.limbo.flowjob.tracker.admin.service.plan;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.dto.job.DispatchOptionDto;
import org.limbo.flowjob.tracker.commons.dto.job.ExecutorOptionDto;
import org.limbo.flowjob.tracker.commons.dto.job.JobDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanAddDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanReplaceDto;
import org.limbo.flowjob.tracker.commons.dto.plan.ScheduleOptionDto;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.ExecutorOption;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanBuilderFactory;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.core.plan.ScheduleOption;
import org.limbo.flowjob.tracker.core.tracker.TrackerNode;
import org.limbo.flowjob.tracker.dao.po.PlanPO;
import org.limbo.flowjob.tracker.infrastructure.plan.repositories.PlanPoRepository;
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
    private TrackerNode trackerNode;

    @Autowired
    private PlanBuilderFactory planBuilderFactory;

    /**
     * 新增计划 只是个落库操作
     */
    public String add(PlanAddDto dto) {
        Plan plan = convertToDo(dto);
        // 保存 plan
        return planRepository.addPlan(plan);
    }

    /**
     * 覆盖计划 可能会触发 内存时间轮改动
     */
    public void replace(String planId, PlanReplaceDto dto) {
        // 获取当前的plan数据
        Plan newVersion = planRepository.newVersion(convertToDo(planId, dto));

        // 需要修改plan重新调度
        if (trackerNode.jobTracker().isScheduling(planId)) {
            trackerNode.jobTracker().unschedule(planId);
            trackerNode.jobTracker().schedule(newVersion);
        }
    }

    /**
     * 启动计划 开始调度 todo 并发
     */
    public void start(String planId) {
        // 校验，计划存在且已启用
        PlanPO planPO = planPoRepository.getById(planId);
        Verifies.notNull(planPO, "plan is not exist");

        if (planPO.getIsEnabled()) {
            return;
        }

        Plan plan = planRepository.getPlan(planId, planPO.getCurrentVersion());
        Verifies.verify(plan.getDag() != null && CollectionUtils.isNotEmpty(plan.getDag().getEarliestJobs()), "job is empty");

        // 更新状态
        planPoRepository.switchEnable(planId, true);

        // 调度 plan
        trackerNode.jobTracker().schedule(plan);
    }

    /**
     * 取消计划 停止调度
     */
    public void stop(String planId) {
        PlanPO planPO = planPoRepository.getById(planId);
        Verifies.notNull(planPO, "plan is not exist");

        if (!planPO.getIsEnabled()) {
            return;
        }

        planPoRepository.switchEnable(planId, false);

        // 停止调度 plan
        trackerNode.jobTracker().unschedule(planId);
    }


    private Plan convertToDo(PlanAddDto dto) {
        return planBuilderFactory.newBuilder()
                .planId(dto.getPlanId())
                .description(dto.getDescription())
                .scheduleOption(convertToDo(dto.getScheduleOption()))
                .jobs(convertToDo(dto.getJobs()))
                .build();
    }

    private Plan convertToDo(String planId, PlanReplaceDto dto) {
        return planBuilderFactory.newBuilder()
                .planId(planId)
                .description(dto.getDescription())
                .scheduleOption(convertToDo(dto.getScheduleOption()))
                .jobs(convertToDo(dto.getJobs()))
                .build();
    }

    private DispatchOption convertToDo(DispatchOptionDto dto) {
        if (dto == null) {
            return null;
        }
        return new DispatchOption(dto.getLoadBalanceType(), dto.getCpuRequirement(), dto.getRamRequirement());
    }

    private ExecutorOption convertToDo(ExecutorOptionDto dto) {
        if (dto == null) {
            return null;
        }
        return new ExecutorOption(dto.getName(), dto.getType());
    }

    private ScheduleOption convertToDo(ScheduleOptionDto dto) {
        if (dto == null) {
            return null;
        }
        return new ScheduleOption(dto.getScheduleType(), dto.getScheduleStartAt(), dto.getScheduleDelay(),
                dto.getScheduleInterval(), dto.getScheduleCron(), dto.getRetry());
    }

    private List<Job> convertToDo(List<JobDto> dtos) {
        List<Job> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(dtos)) {
            return list;
        }
        // 封装对象
        for (JobDto dto : dtos) {
            list.add(convertToDo(dto));
        }
        return list;
    }

    private Job convertToDo(JobDto dto) {
        Job job = new Job();
        job.setJobId(dto.getJobId());
        job.setDescription(dto.getDescription());
        job.setChildrenIds(dto.getChildrenIds());
        job.setDispatchOption(convertToDo(dto.getDispatchOption()));
        job.setExecutorOption(convertToDo(dto.getExecutorOption()));
        return job;
    }

}
