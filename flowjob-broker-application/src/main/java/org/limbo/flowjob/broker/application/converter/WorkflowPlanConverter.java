/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.application.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Setter;
import org.apache.commons.collections4.ListUtils;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.api.dto.console.PlanInfoDTO;
import org.limbo.flowjob.api.dto.console.WorkflowJobDTO;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2023-08-11
 */
@Component
public class WorkflowPlanConverter {


    @Setter(onMethod_ = @Inject)
    private PlanConverter converter;


    /**
     * 转换为 DAG 任务详情 DTO
     * @param planInfo DAG 任务持久化对象
     */
    public PlanInfoDTO.WorkflowPlanInfoDTO toWorkflowPlanDTO(PlanInfoEntity planInfo) {
        PlanInfoDTO.WorkflowPlanInfoDTO dto = new PlanInfoDTO.WorkflowPlanInfoDTO();
        dto.setPlanInfoId(planInfo.getPlanInfoId());
        dto.setPlanId(planInfo.getPlanId());
        dto.setName(planInfo.getName());
        dto.setDescription(planInfo.getDescription());
        dto.setTriggerType(TriggerType.parse(planInfo.getTriggerType()));
        dto.setScheduleOption(converter.toScheduleOptionDTO(planInfo));

        // 填充作业列表
        TypeReference<List<WorkflowJobInfo>> typeRef = new TypeReference<List<WorkflowJobInfo>>() { /* ignore */ };
        List<WorkflowJobInfo> jobs = JacksonUtils.parseObject(planInfo.getJobInfo(), typeRef);
        dto.setWorkflow(ListUtils.emptyIfNull(jobs).stream()
                .map(this::toWorkflowJobDTO)
                .collect(Collectors.toList())
        );

        return dto;
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
        dto.setRetryOption(converter.convertToRetryOption(job.getRetryOption()));
        dto.setDispatchOption(converter.convertJobDispatchOption(job.getDispatchOption()));
        dto.setExecutorName(job.getExecutorName());
        dto.setChildren(job.getChildrenIds());
        dto.setTriggerType(job.getTriggerType());
        dto.setContinueWhenFail(job.isContinueWhenFail());
        return dto;
    }


}
