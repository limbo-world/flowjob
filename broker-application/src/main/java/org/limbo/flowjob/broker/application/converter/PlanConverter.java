/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.application.converter;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.console.param.DispatchOptionParam;
import org.limbo.flowjob.api.console.param.JobParam;
import org.limbo.flowjob.api.console.param.RetryOptionParam;
import org.limbo.flowjob.api.console.param.ScheduleOptionParam;
import org.limbo.flowjob.api.console.param.TagFilterParam;
import org.limbo.flowjob.api.console.param.WorkflowJobParam;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.dispatch.RetryOption;
import org.limbo.flowjob.broker.core.dispatch.TagFilterOption;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Brozen
 * @since 2022-06-11
 */
@Component
public class PlanConverter {

    /**
     * 生成更新计划 JobDAG
     */
    public DAG<WorkflowJobInfo> convertJob(List<WorkflowJobParam> jobParams) {
        return new DAG<>(convertJobs(jobParams));
    }

    public JobInfo covertJob(String id, JobParam jobParam) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setId(id);
        jobInfo.setType(jobParam.getType());
        jobInfo.setAttributes(new Attributes(jobParam.getAttributes()));
        jobInfo.setRetryOption(convertToRetryOption(jobParam.getRetryOption()));
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

        JobInfo jobInfo = covertJob(param.getId(), param);
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
     * 生成作业重试参数
     */
    public RetryOption convertToRetryOption(RetryOptionParam param) {
        if (param == null) {
            return new RetryOption();
        }
        return RetryOption.builder()
                .retry(param.getRetry())
                .retryInterval(param.getRetryInterval())
                .build();
    }

    /**
     * 生成作业分发参数
     */
    public DispatchOption convertJobDispatchOption(DispatchOptionParam param) {
        return DispatchOption.builder()
                .loadBalanceType(param.getLoadBalanceType())
                .cpuRequirement(param.getCpuRequirement())
                .ramRequirement(param.getRamRequirement())
                .tagFilters(covertTagFilterOption(param.getTagFilters()))
                .build();
    }

    public List<TagFilterOption> covertTagFilterOption(List<TagFilterParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return null;
        }
        return params.stream().map(param -> TagFilterOption.builder()
                .tagName(param.getTagName())
                .tagValue(param.getTagValue())
                .condition(param.getCondition())
                .build()).collect(Collectors.toList());
    }

}
