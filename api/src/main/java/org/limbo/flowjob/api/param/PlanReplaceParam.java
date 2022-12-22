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

package org.limbo.flowjob.api.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.common.constants.TriggerType;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Schema(title = "计划覆盖参数")
public class PlanReplaceParam {

    /**
     * 计划描述
     */
    @Schema(title = "计划描述")
    private String description;

    /**
     * 触发方式
     */
    @Schema(title = "触发方式", implementation = Integer.class)
    @NotNull
    private TriggerType triggerType;

    /**
     * 作业计划调度配置参数
     */
    @Schema(title = "作业计划调度配置参数")
    private ScheduleOptionParam scheduleOption;

    /**
     * 此执行计划对应的所有作业
     */
    @Schema(title = "此执行计划对应的所有作业")
    private List<JobAddParam> jobs;

}
