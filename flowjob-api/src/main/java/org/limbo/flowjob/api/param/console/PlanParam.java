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

package org.limbo.flowjob.api.param.console;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.TriggerType;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Schema(title = "任务参数")
public class PlanParam implements Serializable {

    private static final long serialVersionUID = 3349688739542837391L;

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
     * 计划类型
     * @see PlanType
     */
    @NotNull
    @Schema(title = "任务类型", implementation = Integer.class)
    private PlanType planType;

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
    private ScheduleOptionParam scheduleOption;

    /**
     * 此执行计划对应的所有作业
     */
    @Valid
    @Schema(title = "单任务对应的所有作业")
    private JobParam job;

    /**
     * 此执行计划对应的所有作业
     */
    @Schema(title = "工作流对应的所有作业")
    private List<@Valid WorkflowJobParam> workflow;

}
