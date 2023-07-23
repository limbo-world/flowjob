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
import org.limbo.flowjob.api.constants.ScheduleType;

import java.time.LocalDateTime;

/**
 * @author KaiFengCai
 * @since 2023/1/30
 */
@Data
@Schema(title = "任务对象")
public class PlanInstanceDTO {

    @Schema(title = "id")
    private String planInstanceId;

    @Schema(title = "planId")
    private String planId;

    @Schema(title = "版本id")
    private String planInfoId;

    /**
     * 状态
     */
    @Schema(title = "状态")
    private Integer status;

    /**
     * 触发类型
     */
    @Schema(title = "触发类型")
    private Integer triggerType;

    /**
     * 计划作业调度方式
     * @see ScheduleType
     */
    @Schema(title = "计划作业调度方式")
    private Integer scheduleType;

    /**
     * 期望的调度触发时间
     */
    @Schema(title = "期望的调度触发时间")
    private LocalDateTime triggerAt;

    /**
     * 执行开始时间
     */
    @Schema(title = "执行开始时间")
    private LocalDateTime startAt;

    /**
     * 执行结束时间
     */
    @Schema(title = "执行结束时间")
    private LocalDateTime feedbackAt;

}
