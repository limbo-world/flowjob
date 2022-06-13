package org.limbo.flowjob.broker.application.plan.converter;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.param.job.DispatchOptionParam;
import org.limbo.flowjob.broker.api.param.job.ExecutorOptionParam;
import org.limbo.flowjob.broker.api.param.job.JobAddParam;
import org.limbo.flowjob.broker.api.param.plan.PlanAddParam;
import org.limbo.flowjob.broker.api.param.plan.ScheduleOptionParam;
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
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-06-11
 */
@Mapper(uses = PlanConverter.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlanConverter {

    PlanConverter INSTANCE = Mappers.getMapper(PlanConverter.class);


    /**
     * 新增计划参数转换为 执行计划
     */
    @Mapping(target = "planId", source = "planId")
    @Mapping(target = "isEnabled", defaultValue = "false")
    @Mapping(target = "recentlyVersion", ignore = true)
    @Mapping(target = "currentVersion", ignore = true)
    @Mapping(target = "info", expression = "java(new PlanInfo( param.getPlanId(), null, param.getDescription(), param.getScheduleOption(), new JobDAG(param.getJobs()) ))")
    @Mapping(target = "lastScheduleAt", ignore = true)
    @Mapping(target = "lastFeedbackAt", ignore = true)
    @Mapping(target = "planRepository", ignore = true)
    @Mapping(target = "planInfoRepository", ignore = true)
    @Mapping(target = "triggerCalculator", ignore = true)
    @Mapping(target = "eventEventPublisher", ignore = true)
    @Mapping(target = "strategyFactory", ignore = true)
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
    @Mapping(target = "retry", source = "retry")
    ScheduleOption convertScheduleOption(ScheduleOptionParam param);

    /**
     * 转换 作业DAG
     */
    @Named("convertJobDAG")
    default JobDAG convertJobDAG(PlanAddParam param) {
        List<JobAddParam> jobParams = param.getJobs();
        if (CollectionUtils.isEmpty(jobParams)) {
            return new JobDAG(Lists.newArrayList());
        }

        List<Job> jobs = jobParams.stream()
                .map(this::convertJob)
                .collect(Collectors.toList());
        return new JobDAG(jobs);
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
