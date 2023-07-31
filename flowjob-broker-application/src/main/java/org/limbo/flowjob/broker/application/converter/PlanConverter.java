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
import org.limbo.flowjob.api.dto.console.DispatchOptionDTO;
import org.limbo.flowjob.api.dto.console.PlanInfoDTO;
import org.limbo.flowjob.api.dto.console.RetryOptionDTO;
import org.limbo.flowjob.api.dto.console.TagFilterDTO;
import org.limbo.flowjob.api.param.console.DispatchOptionParam;
import org.limbo.flowjob.api.param.console.PlanParam;
import org.limbo.flowjob.api.param.console.RetryOptionParam;
import org.limbo.flowjob.api.param.console.TagFilterParam;
import org.limbo.flowjob.api.param.console.WorkflowJobParam;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.dispatch.RetryOption;
import org.limbo.flowjob.broker.core.dispatch.TagFilterOption;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.springframework.stereotype.Component;

import java.util.Collections;
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

    public JobInfo covertJob(PlanParam.NormalPlanParam jobParam) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setId("0");
        jobInfo.setType(jobParam.getType());
        jobInfo.setExecutorName(jobParam.getExecutorName());
        jobInfo.setAttributes(new Attributes(jobParam.getAttributes()));
        jobInfo.setRetryOption(convertToRetryOption(jobParam.getRetryOption()));
        jobInfo.setDispatchOption(convertJobDispatchOption(jobParam.getDispatchOption()));
        return jobInfo;
    }

    public void assemble(PlanInfoDTO.NormalPlanInfoDTO dto, JobInfo jobInfo) {
        dto.setType(jobInfo.getType());
        dto.setAttributes(jobInfo.getAttributes().toMap());
        dto.setExecutorName(jobInfo.getExecutorName());
        dto.setRetryOption(convertToRetryOption(jobInfo.getRetryOption()));
        dto.setDispatchOption(convertJobDispatchOption(jobInfo.getDispatchOption()));
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
        WorkflowJobInfo jobInfo = new WorkflowJobInfo();
        jobInfo.setId(param.getId());
        jobInfo.setName(param.getName());
        jobInfo.setDescription(param.getDescription());
        jobInfo.setTriggerType(param.getTriggerType());
        jobInfo.setContinueWhenFail(param.isContinueWhenFail());
        jobInfo.setType(param.getType());
        jobInfo.setAttributes(new Attributes(param.getAttributes()));
        jobInfo.setRetryOption(convertToRetryOption(param.getRetryOption()));
        jobInfo.setDispatchOption(convertJobDispatchOption(param.getDispatchOption()));
        jobInfo.setExecutorName(param.getExecutorName());
        return jobInfo;
    }


//    /**
//     * 新增计划参数转换为 计划调度配置
//     */
//    public ScheduleOption convertScheduleOption(ScheduleOptionParam param) {
//        return new ScheduleOption(
//                param.getScheduleType(),
//                param.getScheduleStartAt(),
//                param.getScheduleDelay(),
//                param.getScheduleInterval(),
//                param.getScheduleCron(),
//                param.getScheduleCronType()
//        );
//    }

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
                .retryType(param.getRetryType())
                .build();
    }

    public RetryOptionDTO convertToRetryOption(RetryOption option) {
        if (option == null) {
            return new RetryOptionDTO();
        }
        return RetryOptionDTO.builder()
                .retry(option.getRetry())
                .retryInterval(option.getRetryInterval())
                .retryType(option.getRetryInterval())
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

    public DispatchOptionDTO convertJobDispatchOption(DispatchOption option) {
        return DispatchOptionDTO.builder()
                .loadBalanceType(option.getLoadBalanceType())
                .cpuRequirement(option.getCpuRequirement())
                .ramRequirement(option.getRamRequirement())
                .tagFilters(covertTagFilterOptionDTO(option.getTagFilters()))
                .build();
    }

    public List<TagFilterOption> covertTagFilterOption(List<TagFilterParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return Collections.emptyList();
        }
        return params.stream().map(param -> TagFilterOption.builder()
                .tagName(param.getTagName())
                .tagValue(param.getTagValue())
                .condition(param.getCondition())
                .build()).collect(Collectors.toList());
    }

    public List<TagFilterDTO> covertTagFilterOptionDTO(List<TagFilterOption> options) {
        if (CollectionUtils.isEmpty(options)) {
            return Collections.emptyList();
        }
        return options.stream().map(option -> TagFilterDTO.builder()
                .tagName(option.getTagName())
                .tagValue(option.getTagValue())
                .condition(option.getCondition())
                .build()).collect(Collectors.toList());
    }

}
