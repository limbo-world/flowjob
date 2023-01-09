package org.limbo.flowjob.broker.application.plan.converter;

import com.google.common.collect.Lists;
import org.limbo.flowjob.api.console.param.DispatchOptionParam;
import org.limbo.flowjob.api.console.param.ScheduleOptionParam;
import org.limbo.flowjob.api.console.param.WorkflowJobParam;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.utils.dag.DAG;

import java.util.List;

/**
 * @author Brozen
 * @since 2022-06-11
 */
public class PlanConverter {

    /**
     * 生成更新计划 JobDAG
     */
    public static DAG<JobInfo> convertJob(List<WorkflowJobParam> jobParams) {
        return new DAG<>(convertJobs(jobParams));
    }


    public static List<JobInfo> convertJobs(List<WorkflowJobParam> jobParams) {
        List<JobInfo> joblist = Lists.newArrayList();
        for (WorkflowJobParam jobParam : jobParams) {
            joblist.add(convertJob(jobParam));
        }
        return joblist;

    }


    /**
     * 生成单个作业
     */
    public static JobInfo convertJob(WorkflowJobParam param) {
        JobInfo jobInfo = new JobInfo(param.getName(), param.getChildren());
        jobInfo.setDescription(param.getDescription());
        jobInfo.setTriggerType(param.getTriggerType());
        jobInfo.setType(param.getType());
//        jobInfo.setAttributes(); todo
        jobInfo.setDispatchOption(convertJobDispatchOption(param.getDispatchOption()));
        jobInfo.setExecutorName(param.getExecutorName());
        jobInfo.setTerminateWithFail(param.isTerminateWithFail());
        return jobInfo;
    }


    /**
     * 新增计划参数转换为 计划调度配置
     */
    public static ScheduleOption convertScheduleOption(ScheduleOptionParam param) {
        return new ScheduleOption(
                param.getScheduleType(),
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
        return DispatchOption.builder()
                .loadBalanceType(param.getLoadBalanceType())
                .cpuRequirement(param.getCpuRequirement())
                .ramRequirement(param.getRamRequirement())
                .retry(param.getRetry())
                .retryInterval(param.getRetryInterval())
                .tagFilter(null) // TODO
                .build();
    }

}
