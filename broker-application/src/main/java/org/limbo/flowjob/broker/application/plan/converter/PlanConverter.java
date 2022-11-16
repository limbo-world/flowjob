package org.limbo.flowjob.broker.application.plan.converter;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.BooleanUtils;
import org.limbo.flowjob.api.param.DispatchOptionParam;
import org.limbo.flowjob.api.param.JobAddParam;
import org.limbo.flowjob.api.param.PlanAddParam;
import org.limbo.flowjob.api.param.PlanReplaceParam;
import org.limbo.flowjob.api.param.ScheduleOptionParam;
import org.limbo.flowjob.broker.core.domain.DispatchOption;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanInfo;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.time.TimeUtils;

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

        plan.getInfo().check();

        plan.setCurrentVersion("1");
        plan.setRecentlyVersion("1");
        plan.setNextTriggerAt(TimeUtils.currentLocalDateTime());

        return plan;
    }


    /**
     * 生成新增计划时的计划数据
     */
    public static PlanInfo convertPlanInfo(PlanAddParam param) {
        // 一些默认值的设置
        ScheduleOptionParam scheduleOption = param.getScheduleOption();
        if (scheduleOption.getScheduleStartAt() == null) {
            param.getScheduleOption().setScheduleStartAt(TimeUtils.currentLocalDateTime());
        }
        return new PlanInfo(null, null, param.getDescription(), convertScheduleOption(scheduleOption), convertJob(param.getJobs()));
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
    public static DAG<JobInfo> convertJob(List<JobAddParam> jobAddParams) {
        return new DAG<>(convertJobs(jobAddParams));
    }


    public static List<JobInfo> convertJobs(List<JobAddParam> jobAddParams) {
        List<JobInfo> joblist = Lists.newArrayList();
        for (JobAddParam jobAddParam : jobAddParams) {
            joblist.add(convertJob(jobAddParam));
        }
        return joblist;

    }


    /**
     * 生成单个作业
     */
    public static JobInfo convertJob(JobAddParam param) {
        JobInfo jobInfo = new JobInfo(param.getJobId(), param.getChildrenIds());
        jobInfo.setName(param.getJobName());
        jobInfo.setDescription(param.getDescription());
        jobInfo.setTriggerType(param.getTriggerType());
        jobInfo.setType(param.getType());
//        jobInfo.setAttributes(); todo
        jobInfo.setDispatchOption(convertJobDispatchOption(param.getDispatchOption()));
        jobInfo.setExecutorName(param.getExecutorName());
        jobInfo.setTerminateWithFail(BooleanUtils.isFalse(param.getTerminateWithFail()));
        return jobInfo;
    }


    /**
     * 新增计划参数转换为 计划调度配置
     */
    public static ScheduleOption convertScheduleOption(ScheduleOptionParam param) {
        return new ScheduleOption(
                param.getScheduleType(),
                param.getTriggerType(),
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
        return new DispatchOption(param.getLoadBalanceType(),
                param.getCpuRequirement(),
                param.getRamRequirement(),
                param.getRetry(),
                param.getRetryInterval()
        );
    }

}
