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

package org.limbo.flowjob.api.dto.console;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.constants.TriggerType;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Devil
 * @since 2023/7/12
 */
@Data
public class PlanInfoDTO implements Serializable {

    private static final long serialVersionUID = 5415937110190483426L;

    /**
     * 计划ID
     */
    @Schema(title = "任务ID")
    private String planInfoId;

    /**
     * 计划ID
     */
    @Schema(title = "任务ID")
    private String planId;

    /**
     * 计划名称
     */
    @Schema(title = "任务名称")
    private String name;

    /**
     * 计划描述
     */
    @Schema(title = "任务描述")
    private String description;

    /**
     * 触发方式
     * @see TriggerType
     */
    @NotNull
    @Schema(title = "触发方式", implementation = Integer.class)
    private TriggerType triggerType;

    /**
     * 作业计划调度配置参数
     */
    @NotNull
    @Schema(title = "调度配置参数")
    private ScheduleOptionDTO scheduleOption;

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(title = "普通任务参数")
    public static class NormalPlanInfoDTO extends PlanInfoDTO {

        private static final long serialVersionUID = 3895596530765470400L;

        /**
         * 作业类型
         * @see JobType
         */
        @NotNull
        @Schema(title = "作业类型")
        private JobType type;

        /**
         * 属性参数
         */
        @Schema(title = "属性参数")
        private Map<String, Object> attributes;

        /**
         * 作业超时参数
         */
        @Schema(title = "作业超时参数")
        private OvertimeOptionDTO overtimeOption;

        /**
         * 作业分发重试参数
         */
        @Schema(title = "作业分发重试参数")
        private RetryOptionDTO retryOption;

        /**
         * 作业分发配置参数
         */
        @Valid
        @NotNull
        @Schema(title = "作业分发配置参数")
        private DispatchOptionDTO dispatchOption;

        /**
         * 执行器名称
         */
        @NotBlank
        @Schema(title = "执行器名称")
        private String executorName;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(title = "工作流任务参数")
    public static class WorkflowPlanInfoDTO extends PlanInfoDTO {

        private static final long serialVersionUID = 7590151688905915258L;

        /**
         * 此执行计划对应的所有作业
         */
        @Schema(title = "工作流对应的所有作业")
        private List<@Valid WorkflowJobDTO> workflow;
    }

}
