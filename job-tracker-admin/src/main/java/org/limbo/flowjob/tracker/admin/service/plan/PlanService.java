package org.limbo.flowjob.tracker.admin.service.plan;

import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.console.param.DispatchOptionParam;
import org.limbo.flowjob.broker.api.console.param.ExecutorOptionParam;
import org.limbo.flowjob.broker.api.console.param.JobAddParam;
import org.limbo.flowjob.broker.api.console.param.PlanAddParam;
import org.limbo.flowjob.broker.api.console.param.PlanReplaceParam;
import org.limbo.flowjob.broker.api.console.param.ScheduleOptionParam;
import org.limbo.flowjob.broker.core.broker.TrackerNode;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.PlanScheduler;
import org.limbo.flowjob.broker.core.plan.job.DispatchOption;
import org.limbo.flowjob.broker.core.plan.job.ExecutorOption;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.JobDAG;
import org.limbo.flowjob.broker.core.repositories.PlanRepository;
import org.limbo.flowjob.broker.core.repositories.PlanSchedulerRepository;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.utils.verifies.Verifies;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Service
public class PlanService {

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    @Setter(onMethod_ = @Inject)
    private PlanSchedulerRepository planSchedulerRepository;

    @Setter(onMethod_ = @Inject)
    private TrackerNode trackerNode;

    /**
     * 新增计划 只是个落库操作
     */
    public String add(PlanAddParam dto) {
        Plan plan = convertToPlan(dto);
        return planRepository.addPlan(plan);
    }


    /**
     * 覆盖计划 可能会触发 内存时间轮改动
     */
    @Transactional
    public void replace(String planId, PlanReplaceParam dto) {
        // 获取当前的plan数据
        Plan plan = planRepository.get(planId);
        Verifies.notNull(plan, "plan not exist");

        // todo plan 创建新版本 并切换内存中的信息
        // 更新版本数据
        PlanInfo planInfo = new PlanInfo(planId, null, dto.getDescription(), convertToVo(dto.getScheduleOption()), new JobDAG(convertToDo(dto.getJobs())));
        String version = plan.addNewVersion(planInfo);

        PlanScheduler planScheduler = planSchedulerRepository.get(version);

        // 需要修改plan重新调度
        if (trackerNode.jobTracker().isScheduling(planId)) {
            trackerNode.jobTracker().unschedule(planId);
            trackerNode.jobTracker().schedule(planScheduler);
        }
    }

    /**
     * 启动计划 开始调度 todo 并发
     */
    public void start(String planId) {
        // 校验，计划存在且已启用
        Plan plan = planRepository.get(planId);
        Verifies.notNull(plan, "plan is not exist");

        // 已经启动不重复处理
        if (plan.isEnabled()) {
            return;
        }

        // 获取当前版本的计划信息，并校验Jobs
        PlanInfo planInfo = plan.getInfo();
        Verifies.notNull(planInfo, "version info not exist");
        JobDAG dag = planInfo.getDag();
        Verifies.notNull(dag, "jobs not exist");
        Verifies.notEmpty(dag.getEarliestJobs(), "jobs is empty");

        // 更新状态
        if (plan.enable()) {
            // 调度 plan
            PlanScheduler planScheduler = planSchedulerRepository.get(plan.getCurrentVersion());
            trackerNode.jobTracker().schedule(planScheduler);
        }
    }


    /**
     * 取消计划 停止调度
     */
    public void stop(String planId) {
        // 查询计划
        Plan plan = planRepository.get(planId);
        Verifies.notNull(plan, "plan is not exist");

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


    /**
     * 将新增执行计划dto转换为Plan领域对象
     * FIXME 是否考虑抽取converter
     * @param dto 新增执行计划dto
     * @return Plan领域对象
     */
    private Plan convertToPlan(PlanAddParam dto) {
        // Plan dto 转 do
        Plan plan = new Plan();
//        plan.setPlanId(dto.getPlanId()); // todo 数据注入
        plan.setEnabled(false);

        return plan;
    }


    private DispatchOption convertToDo(DispatchOptionParam dto) {
        if (dto == null) {
            return null;
        }
        return new DispatchOption(dto.getLoadBalanceType(), dto.getCpuRequirement(), dto.getRamRequirement(), dto.getRetry());
    }

    private ExecutorOption convertToDo(ExecutorOptionParam dto) {
        if (dto == null) {
            return null;
        }
        return new ExecutorOption(dto.getName(), dto.getType());
    }

    private ScheduleOption convertToVo(ScheduleOptionParam dto) {
        if (dto == null) {
            return null;
        }
        return new ScheduleOption(dto.getScheduleType(), dto.getScheduleStartAt(), dto.getScheduleDelay(),
                dto.getScheduleInterval(), dto.getScheduleCron(), dto.getScheduleCronType());
    }

    private List<Job> convertToDo(List<JobAddParam> dtos) {
        List<Job> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(dtos)) {
            return list;
        }
        // 封装对象
        for (JobAddParam dto : dtos) {
            list.add(convertToDo(dto));
        }
        return list;
    }

    private Job convertToDo(JobAddParam dto) {
        Job job = new Job();
        job.setJobId(dto.getJobId());
        job.setDescription(dto.getDescription());
        job.setChildrenIds(dto.getChildrenIds());
        job.setDispatchOption(convertToDo(dto.getDispatchOption()));
        job.setExecutorOption(convertToDo(dto.getExecutorOption()));
        return job;
    }

}
