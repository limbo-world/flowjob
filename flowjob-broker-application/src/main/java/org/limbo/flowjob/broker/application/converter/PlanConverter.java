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

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.ScheduleType;
import org.limbo.flowjob.api.dto.console.DispatchOptionDTO;
import org.limbo.flowjob.api.dto.console.PlanInfoDTO;
import org.limbo.flowjob.api.dto.console.RetryOptionDTO;
import org.limbo.flowjob.api.dto.console.ScheduleOptionDTO;
import org.limbo.flowjob.api.dto.console.TagFilterDTO;
import org.limbo.flowjob.api.dto.console.WorkflowJobDTO;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.worker.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.worker.dispatch.RetryOption;
import org.limbo.flowjob.broker.core.worker.dispatch.TagFilterOption;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
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
     * 填充作业信息到作业 DTO 中，非 DAG 作业
     */
    public void assemble(PlanInfoDTO.NormalPlanInfoDTO dto, JobInfo jobInfo) {
        dto.setType(jobInfo.getType());
        dto.setAttributes(jobInfo.getAttributes().toMap());
        dto.setExecutorName(jobInfo.getExecutorName());
        dto.setRetryOption(convertToRetryOption(jobInfo.getRetryOption()));
        dto.setDispatchOption(convertJobDispatchOption(jobInfo.getDispatchOption()));
    }


    /**
     * 转换为单个 DAG 作业 DTO
     */
    public WorkflowJobDTO toWorkflowJobDTO(WorkflowJobInfo job) {
        WorkflowJobDTO dto = new WorkflowJobDTO();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setDescription(job.getDescription());
        dto.setType(job.getType());
        dto.setAttributes(job.getAttributes().toMap());
        dto.setRetryOption(convertToRetryOption(job.getRetryOption()));
        dto.setDispatchOption(convertJobDispatchOption(job.getDispatchOption()));
        dto.setExecutorName(job.getExecutorName());
        dto.setChildren(job.getChildrenIds());
        dto.setTriggerType(job.getTriggerType());
        dto.setContinueWhenFail(job.isContinueWhenFail());
        return dto;
    }


    /**
     * 转换为任务调度配置 DTO
     * @param planInfo 任务持久化对象
     */
    public ScheduleOptionDTO toScheduleOptionDTO(PlanInfoEntity planInfo) {
        ScheduleOptionDTO dto = new ScheduleOptionDTO();
        dto.setScheduleType(ScheduleType.parse(planInfo.getScheduleType()));
        dto.setScheduleStartAt(planInfo.getScheduleStartAt());
        dto.setScheduleEndAt(planInfo.getScheduleEndAt());
        dto.setScheduleDelay(planInfo.getScheduleDelay());
        dto.setScheduleInterval(planInfo.getScheduleInterval());
        dto.setScheduleCron(planInfo.getScheduleCron());
        dto.setScheduleCronType(planInfo.getScheduleCronType());
        return dto;
    }


    /**
     * 转换为作业重试 DTO
     */
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
     * 转换为作业分发参数 DTO
     */
    public DispatchOptionDTO convertJobDispatchOption(DispatchOption option) {
        return DispatchOptionDTO.builder()
                .loadBalanceType(option.getLoadBalanceType())
                .cpuRequirement(option.getCpuRequirement())
                .ramRequirement(option.getRamRequirement())
                .tagFilters(covertTagFilterOptionDTO(option.getTagFilters()))
                .build();
    }


    /**
     * 转换为作业过滤标签 DTO
     */
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