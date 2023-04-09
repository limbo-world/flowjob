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

package org.limbo.flowjob.api.console.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TriggerType;

import java.time.LocalDateTime;

/**
 * @author KaiFengCai
 * @since 2023/1/30
 */
@Data
@Schema(title = "任务对象")
public class PlanVO {

    @Schema(title = "id")
    private String planId;

    @Schema(title = "当前版本")
    private String currentVersion;

    @Schema(title = "最新版本")
    private String recentlyVersion;

    @Schema(title = "是否启动")
    private boolean enabled;

    @Schema(title = "名称")
    private String name;

    @Schema(title = "描述")
    private String description;

    /**
     * 计划作业类型
     * @see PlanType
     */
    @Schema(title = "任务类型")
    private Byte planType;

    /**
     * 计划作业调度方式
     * @see ScheduleType
     */
    @Schema(title = "调度方式")
    private Byte scheduleType;

    /**
     * 计划作业触发方式
     * @see TriggerType
     */
    @Schema(title = "触发方式")
    private Byte triggerType;

    /**
     * 从何时开始调度作业
     */
    @Schema(title = "调度开始时间")
    private LocalDateTime scheduleStartAt;

    /**
     * 作业调度延迟时间，单位秒
     */
    @Schema(title = "调度延迟时间")
    private Long scheduleDelay;

    /**
     * 作业调度间隔时间，单位秒。
     */
    @Schema(title = "调度间隔时间")
    private Long scheduleInterval;

    /**
     * 作业调度的CRON表达式
     */
    @Schema(title = "CRON表达式")
    private String scheduleCron;

    /**
     * 作业调度的CRON表达式
     * @see com.cronutils.model.CronType
     */
    @Schema(title = "CRON表达式类型")
    private String scheduleCronType;

    @Schema(title = "重试次数")
    private Integer retry;
}
