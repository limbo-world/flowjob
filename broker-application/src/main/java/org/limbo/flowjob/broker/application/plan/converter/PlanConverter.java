package org.limbo.flowjob.broker.application.plan.converter;

import org.limbo.flowjob.broker.api.console.param.*;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.job.DispatchOption;
import org.limbo.flowjob.broker.core.plan.job.ExecutorOption;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.JobDAG;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Brozen
 * @since 2022-06-11
 */
@Mapper(
        uses = PlanConverter.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        imports = {PlanInfo.class, JobDAG.class}
)
public abstract class PlanConverter {


    /**
     * 新增计划
     */
    @Mapping(target = "enabled", constant = "false")
    @Mapping(source = "param", target = "info",qualifiedByName = "convertPlanInfo")
    public abstract Plan convertPlan(PlanAddParam param);


    /**
     * 生成新增计划时的计划数据
     */
    @Named("convertPlanInfo")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "scheduleOption", target = "scheduleOption", qualifiedByName = "convertScheduleOption")
    @Mapping(source = "param", target = "dag", qualifiedByName = "convertJobForAdd")
    public abstract PlanInfo convertPlanInfo(PlanAddParam param);

    /**
     * 生成新增计划 JobDAG
     */
    @Named("convertJobForAdd")
    @Mapping(source = "jobs", target = "jobs", qualifiedByName = "convertJobs")
    public abstract JobDAG convertJob(PlanAddParam param);


    /**
     * 生成新增计划时的计划数据
     */
    @Named("convertPlanInfo")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "scheduleOption", target = "scheduleOption", qualifiedByName = "convertScheduleOption")
    @Mapping(source = "param", target = "dag", qualifiedByName = "convertJobForReplace")
    public abstract PlanInfo convertPlanInfo(PlanReplaceParam param);

    /**
     * 生成更新计划 JobDAG
     */
    @Named("convertJobForReplace")
    @Mapping(source = "jobs", target = "jobs", qualifiedByName = "convertJobs")
    public abstract JobDAG convertJob(PlanReplaceParam param);


    @Named("convertJobs")
    public abstract List<Job> convertJobs(List<JobAddParam> jobAddParams);


    /**
     * 生成单个作业
     */
    @Mapping(target = "jobId", source = "jobId")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "childrenIds", source = "childrenIds")
    @Mapping(target = "dispatchOption", source = "dispatchOption", qualifiedByName = "convertJobDispatchOption")
    @Mapping(target = "executorOption", qualifiedByName = "convertJobExecutorOption")
    public abstract Job convertJob(JobAddParam param);


    /**
     * 新增计划参数转换为 计划调度配置
     */
    @Named("convertScheduleOption")
    @Mapping(target = "scheduleType", source = "scheduleType")
    @Mapping(target = "scheduleStartAt", source = "scheduleStartAt")
    @Mapping(target = "scheduleDelay", source = "scheduleDelay")
    @Mapping(target = "scheduleInterval", source = "scheduleInterval")
    @Mapping(target = "scheduleCron", source = "scheduleCron")
    @Mapping(target = "scheduleCronType", source = "scheduleCronType")
    public abstract ScheduleOption convertScheduleOption(ScheduleOptionParam param);


    /**
     * 生成作业分发参数
     */
    @Named("convertJobDispatchOption")
    @Mapping(target = "loadBalanceType", source = "loadBalanceType")
    @Mapping(target = "cpuRequirement", source = "cpuRequirement")
    @Mapping(target = "ramRequirement", source = "ramRequirement")
    public abstract DispatchOption convertJobDispatchOption(DispatchOptionParam param);


    /**
     * 生成作业调度参数
     */
    @Named("convertJobExecutorOption")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "type", source = "type")
    public abstract ExecutorOption convertJobExecutorOption(ExecutorOptionParam param);

}
