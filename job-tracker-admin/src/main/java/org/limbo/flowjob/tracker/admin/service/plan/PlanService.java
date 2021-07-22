package org.limbo.flowjob.tracker.admin.service.plan;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.dto.job.JobAddDto;
import org.limbo.flowjob.tracker.commons.dto.job.JobUpdateDto;
import org.limbo.flowjob.tracker.commons.dto.plan.DispatchOptionDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanAddDto;
import org.limbo.flowjob.tracker.commons.dto.plan.ScheduleOptionDto;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.JobRepository;
import org.limbo.flowjob.tracker.core.job.ScheduleOption;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.tracker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.tracker.core.schedule.scheduler.HashedWheelTimerScheduler;
import org.limbo.flowjob.tracker.dao.po.PlanPO;
import org.limbo.flowjob.tracker.infrastructure.plan.converters.PlanPoConverter;
import org.limbo.flowjob.tracker.infrastructure.plan.repositories.PlanPoRepository;
import org.limbo.utils.UUIDUtils;
import org.limbo.utils.verifies.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private ScheduleCalculatorFactory scheduleCalculatorFactory;

    @Autowired
    private JobInstanceRepository jobInstanceRepository;

    @Autowired
    private JobRepository jobRepository;

    /**
     * 新增计划 只是个落库操作
     * @param dto
     * @return
     */
    public String add(PlanAddDto dto) {
        return planRepository.addPlan(convertToDo(dto));
    }

    /**
     * 修改计划 可能会触发 内存时间轮改动
     */
    public void update(String planId, String planDesc, ScheduleOptionDto scheduleOption, List<JobAddDto> addJobs, List<JobUpdateDto> updateJobs, List<String> deleteJobIds) {
        // 前置校验
        if (CollectionUtils.isNotEmpty(updateJobs)) {
            for (JobUpdateDto updateJob : updateJobs) {
                if (StringUtils.isBlank(updateJob.getJobId())) {
                    throw new IllegalArgumentException("update job need jobId");
                }
            }
        }

        // 修改plan数据
        planRepository.updatePlan(planId, planDesc, convertToDo(scheduleOption));

        // 修改job数据 先删后增
        if (needResetJob(addJobs, updateJobs, deleteJobIds)) {
            // 获取旧的 job 数据  并删除
            List<Job> jobs = jobRepository.getUsedJobsByPlan(planId);
            jobRepository.deleteUsedJobsByPlan(planId);

            // 添加 -》更新  —》 删除
            jobs.addAll(convertToDo(planId, addJobs));
            if (CollectionUtils.isNotEmpty(updateJobs)) {
                jobs = jobs.stream().peek(job-> {
                    for (JobUpdateDto updateJob : updateJobs) {
                        if (!job.getJobId().equals(updateJob.getJobId())) {
                            continue;
                        }
                        if (StringUtils.isNotBlank(updateJob.getJobDesc())) {
                            job.setJobDesc(updateJob.getJobDesc());
                        }
                        if (CollectionUtils.isNotEmpty(updateJob.getParentJobIds())) {
                            job.setParentJobIds(updateJob.getParentJobIds());
                        }
                        if (updateJob.getDispatchOption() == null) {
                            continue;
                        }
                        if (updateJob.getDispatchOption().getDispatchType() != null) {
                            job.setDispatchOption(job.getDispatchOption().setDispatchType(updateJob.getDispatchOption().getDispatchType()));
                        }
                        if (updateJob.getDispatchOption().getCpuRequirement() != null) {
                            job.setDispatchOption(job.getDispatchOption().setCpuRequirement(updateJob.getDispatchOption().getCpuRequirement()));
                        }
                        if (updateJob.getDispatchOption().getRamRequirement() != null) {
                            job.setDispatchOption(job.getDispatchOption().setRamRequirement(updateJob.getDispatchOption().getRamRequirement()));
                        }
                    }
                }).collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(deleteJobIds)) {
                jobs = jobs.stream().filter(job -> !deleteJobIds.contains(job.getJobId())).collect(Collectors.toList());
            }
            jobRepository.batchInsert(jobs);
        }

        // 需要修改plan重新调度
        if ((scheduleOption != null || needResetJob(addJobs, updateJobs, deleteJobIds)) && scheduler.isScheduling(planId)) {
            Plan plan = planRepository.getPlan(planId);
            scheduler.unschedule(planId);
            scheduler.schedule(plan);
        }
    }

    private boolean needResetJob(List<JobAddDto> addJobs, List<JobUpdateDto> updateJobs, List<String> deleteJobIds) {
        return CollectionUtils.isNotEmpty(addJobs) || CollectionUtils.isNotEmpty(updateJobs) || CollectionUtils.isNotEmpty(deleteJobIds);
    }

    /**
     * 启动计划 开始调度
     * @param planId
     */
    public void enable(String planId) {
        PlanPO planPO = planPoRepository.getById(planId);
        Verifies.notNull(planPO, "计划不存在");

        if (planPO.getIsEnabled() || planPO.getIsDeleted()) {
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

        if (planPO.getIsEnabled() || planPO.getIsDeleted()) {
            return;
        }

        planPoRepository.switchEnable(planId, false);

        // 停止调度 plan
        scheduler.unschedule(planId);
    }


    public Plan convertToDo(PlanAddDto dto) {

        ScheduleType scheduleType = dto.getScheduleOption().getScheduleType();
        ScheduleCalculator scheduleCalculator = scheduleCalculatorFactory.newStrategy(scheduleType);

        Plan plan = new Plan(scheduleCalculator);
        plan.setPlanId(dto.getPlanId());
        // ID未设置则生成一个
        if (StringUtils.isBlank(plan.getPlanId())) {
            plan.setPlanId(UUIDUtils.randomID());
        }
        plan.setPlanDesc(dto.getPlanDesc());
        plan.setScheduleOption(convertToDo(dto.getScheduleOption()));
        plan.setJobs(convertToDo(plan.getPlanId(), dto.getJobs()));

        return plan;
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

    public List<Job> convertToDo(String planId, List<JobAddDto> dtos) {
        List<Job> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(dtos)) {
            return list;
        }
        for (JobAddDto dto : dtos) {
            list.add(convertToDo(planId, dto));
        }
        return list;
    }

    public Job convertToDo(String planId, JobAddDto dto) {
        Job job = new Job(jobInstanceRepository);
        job.setPlanId(planId);
        job.setJobId(dto.getJobId());
        // ID未设置则生成一个
        if (StringUtils.isBlank(job.getPlanId())) {
            job.setPlanId(UUIDUtils.randomID());
        }
        job.setJobDesc(dto.getJobDesc());
        job.setParentJobIds(dto.getParentJobIds());
        job.setDispatchOption(convertToDo(dto.getDispatchOption()));
        return job;
    }


}
