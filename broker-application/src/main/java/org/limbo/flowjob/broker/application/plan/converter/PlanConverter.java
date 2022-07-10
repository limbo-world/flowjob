package org.limbo.flowjob.broker.application.plan.converter;

import com.google.common.collect.Lists;
import org.limbo.flowjob.broker.api.console.param.DispatchOptionParam;
import org.limbo.flowjob.broker.api.console.param.ExecutorOptionParam;
import org.limbo.flowjob.broker.api.console.param.JobAddParam;
import org.limbo.flowjob.broker.api.console.param.PlanAddParam;
import org.limbo.flowjob.broker.api.console.param.PlanReplaceParam;
import org.limbo.flowjob.broker.api.console.param.ScheduleOptionParam;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.job.DispatchOption;
import org.limbo.flowjob.broker.core.plan.job.ExecutorOption;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.JobDAG;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;

import java.util.List;

/**
 * @author Brozen
 * @since 2022-06-11
 */
public class PlanConverter {


    /**
     * 新增计划
     */
    public static Plan convertPlan(PlanAddParam param) {
        Plan plan = new Plan();
        plan.setInfo(convertPlanInfo(param));
        return plan;
    }


    /**
     * 生成新增计划时的计划数据
     */
    public static PlanInfo convertPlanInfo(PlanAddParam param) {
        return new PlanInfo(null, null, param.getDescription(), convertScheduleOption(param.getScheduleOption()), convertJob(param.getJobs()));
    }


    /**
     * 生成新增计划时的计划数据
     */
    public static PlanInfo convertPlanInfo(PlanReplaceParam param) {
        return new PlanInfo(null, null, param.getDescription(), convertScheduleOption(param.getScheduleOption()), convertJob(param.getJobs()));
    }

    /**
     * 生成更新计划 JobDAG
     */
    public static JobDAG convertJob(List<JobAddParam> jobAddParams) {
        return new JobDAG(convertJobs(jobAddParams));
    }


    public static List<Job> convertJobs(List<JobAddParam> jobAddParams) {
        List<Job> joblist=Lists.newArrayList();
        for (JobAddParam jobAddParam :jobAddParams) {
        	joblist.add(convertJob(jobAddParam));
        }
        return joblist;

    }


    /**
     * 生成单个作业
     */
    public static Job convertJob(JobAddParam param) {
        Job job = new Job();
        job.setJobId(param.getJobId());
        job.setDescription(param.getDescription());
        job.setChildrenIds(param.getChildrenIds());
        job.setDispatchOption(convertJobDispatchOption(param.getDispatchOption()));
        job.setExecutorOption(convertJobExecutorOption(param.getExecutorOption()));
        return job;

    }


    /**
     * 新增计划参数转换为 计划调度配置
     */
    public static ScheduleOption convertScheduleOption(ScheduleOptionParam param) {
            return new ScheduleOption(param.getScheduleType(),
                    param.getScheduleStartAt(),
                    param.getScheduleDelay(),
                    param.getScheduleInterval(),
                    param.getScheduleCron(),
                    param.getScheduleCronType()
            );
    }


    /**
     * 生成作业分发参数
     */
    public static DispatchOption convertJobDispatchOption(DispatchOptionParam param) {
        return new DispatchOption(param.getLoadBalanceType(), param.getCpuRequirement(), param.getRamRequirement(), param.getRetry());

    }


    /**
     * 生成作业调度参数
     */
    public static ExecutorOption convertJobExecutorOption(ExecutorOptionParam param) {
        return new ExecutorOption(param.getName(), param.getType());
    }

}
