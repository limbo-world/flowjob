package org.limbo.flowjob.broker.application.plan.converter;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.console.param.DispatchOptionParam;
import org.limbo.flowjob.broker.api.console.param.ExecutorOptionParam;
import org.limbo.flowjob.broker.api.console.param.JobAddParam;
import org.limbo.flowjob.broker.api.console.param.PlanAddParam;
import org.limbo.flowjob.broker.api.console.param.ScheduleOptionParam;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.job.DispatchOption;
import org.limbo.flowjob.broker.core.plan.job.ExecutorOption;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.JobDAG;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-06-11
 */
@Mapper(uses = PlanConverter.class, unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {PlanInfo.class, JobDAG.class})
public interface PlanConverter {

    PlanConverter INSTANCE = Mappers.getMapper(PlanConverter.class);


    /**
     * 新增计划参数转换为 执行计划
     */
    @Mapping(target = "enabled", constant = "false")
//    @Mapping(target = "info", expression = "java(new PlanInfo( null, null, param.getDescription(), param.getScheduleOption(), new JobDAG(param.getJobs()) ))")
    @Mapping(target = "info", source = "param", qualifiedByName = "convertPlanInfo")
    Plan convertPlan(PlanAddParam param);


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
    ScheduleOption convertScheduleOption(ScheduleOptionParam param);


    @Named("convertJob")
    default JobDAG convertJob(List<JobAddParam> jobAddParams) {
        if (CollectionUtils.isEmpty(jobAddParams)) {
            return new JobDAG(Lists.newArrayList());
        }

        List<Job> jobs = jobAddParams.stream()
                .map(this::convertJob)
                .collect(Collectors.toList());
        return new JobDAG(jobs);
    }

    @Named("convertPlanInfo")
    default PlanInfo convertPlanInfo(PlanAddParam param) {
        return new PlanInfo(null, null, param.getDescription(), convertScheduleOption(param.getScheduleOption()), convertJob(param.getJobs()));
    }


    /**
     * 转换 单个作业
     */
    @Mapping(target = "jobId", source = "jobId")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "childrenIds", source = "childrenIds")
    @Mapping(target = "dispatchOption", source = "dispatchOption", qualifiedByName = "convertJobDispatchOption")
    @Mapping(target = "executorOption", qualifiedByName = "convertJobExecutorOption")
    Job convertJob(JobAddParam param);


    /**
     * 转换 作业分发参数
     */
    @Named("convertJobDispatchOption")
    @Mapping(target = "loadBalanceType", source = "loadBalanceType")
    @Mapping(target = "cpuRequirement", source = "cpuRequirement")
    @Mapping(target = "ramRequirement", source = "ramRequirement")
    DispatchOption convertJobDispatchOption(DispatchOptionParam param);


    /**
     * 转换 作业调度参数
     */
    @Named("convertJobExecutorOption")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "type", source = "type")
    ExecutorOption convertJobExecutorOption(ExecutorOptionParam param);

}
