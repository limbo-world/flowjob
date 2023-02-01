package org.limbo.flowjob.broker.application.plan.converter;

import com.google.common.collect.Lists;
import lombok.Setter;
import org.limbo.flowjob.api.console.param.DispatchOptionParam;
import org.limbo.flowjob.api.console.param.JobParam;
import org.limbo.flowjob.api.console.param.ScheduleOptionParam;
import org.limbo.flowjob.api.console.param.WorkflowJobParam;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.dao.entity.JobInfoEntity;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Brozen
 * @since 2022-06-11
 */
@Component
public class PlanConverter {

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    /**
     * 生成更新计划 JobDAG
     */
    public DAG<WorkflowJobInfo> convertJob(List<WorkflowJobParam> jobParams) {
        return new DAG<>(convertJobs(jobParams));
    }

    public JobInfoEntity toJobInfoEntity(JobInfo jobInfo) {
        JobInfoEntity jobInfoEntity = new JobInfoEntity();
        jobInfoEntity.setJobInfoId(jobInfo.getId());
        jobInfoEntity.setType(jobInfo.getType().type);
        jobInfoEntity.setAttributes(JacksonUtils.toJSONString(jobInfo.getAttributes(), JacksonUtils.DEFAULT_NONE_OBJECT));
        jobInfoEntity.setDispatchOption(JacksonUtils.toJSONString(jobInfo.getDispatchOption(), JacksonUtils.DEFAULT_NONE_OBJECT));
        jobInfoEntity.setExecutorName(jobInfo.getExecutorName());
        return jobInfoEntity;
    }

    public JobInfo covertJob(JobParam jobParam) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setId(idGenerator.generateId(IDType.JOB_INFO));
        jobInfo.setType(jobParam.getType());
        jobInfo.setAttributes(new Attributes(jobParam.getAttributes()));
        jobInfo.setDispatchOption(convertJobDispatchOption(jobParam.getDispatchOption()));
        jobInfo.setExecutorName(jobParam.getExecutorName());
        return jobInfo;
    }

    public List<WorkflowJobInfo> convertJobs(List<WorkflowJobParam> jobParams) {
        List<WorkflowJobInfo> joblist = Lists.newArrayList();
        for (WorkflowJobParam jobParam : jobParams) {
            joblist.add(convertJob(jobParam));
        }
        return joblist;

    }


    /**
     * 生成单个作业
     */
    public WorkflowJobInfo convertJob(WorkflowJobParam param) {
        WorkflowJobInfo workflowJobInfo = new WorkflowJobInfo(param.getId(), param.getChildren());
        workflowJobInfo.setName(param.getName());
        workflowJobInfo.setDescription(param.getDescription());
        workflowJobInfo.setTriggerType(param.getTriggerType());
        workflowJobInfo.setTerminateWithFail(param.isTerminateWithFail());

        JobInfo jobInfo = new JobInfo();
        jobInfo.setId(param.getId());
        jobInfo.setType(param.getType());
//        jobInfo.setAttributes(); todo v1
        jobInfo.setDispatchOption(convertJobDispatchOption(param.getDispatchOption()));
        jobInfo.setExecutorName(param.getExecutorName());
        workflowJobInfo.setJob(jobInfo);
        return workflowJobInfo;
    }


    /**
     * 新增计划参数转换为 计划调度配置
     */
    public ScheduleOption convertScheduleOption(ScheduleOptionParam param) {
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
    public DispatchOption convertJobDispatchOption(DispatchOptionParam param) {
        return DispatchOption.builder()
                .loadBalanceType(param.getLoadBalanceType())
                .cpuRequirement(param.getCpuRequirement())
                .ramRequirement(param.getRamRequirement())
                .retry(param.getRetry())
                .retryInterval(param.getRetryInterval())
                .tagFilter(null) // TODO v1
                .build();
    }

}
