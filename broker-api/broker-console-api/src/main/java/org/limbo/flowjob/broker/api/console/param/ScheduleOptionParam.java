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

package org.limbo.flowjob.broker.api.console.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@Schema(title = "作业计划调度配置参数")
public class ScheduleOptionParam {

    /**
     * 作业调度方式
     */
    @Schema(title = "调度方式", implementation = Integer.class, description = ScheduleType.DESCRIPTION)
    private ScheduleType scheduleType;

    /**
     * 作业调度开始时间，从此时间开始执行调度。
     */
    @Schema(title = "调度开始时间")
    private LocalDateTime scheduleStartAt;

    /**
     * 延迟时间 -- 当前时间多久后调度
     */
    @Schema(title = "延迟时间",
            implementation = Float.class,
            description = "延迟时间 -- 当前时间多久后调度"
    )
    private Duration scheduleDelay;

    /**
     * 获取调度间隔时间。
     * 当调度方式为{@link ScheduleType#FIXED_DELAY}时，表示前一次作业调度执行完成后，隔多久触发第二次调度。
     * 当调度方式为{@link ScheduleType#FIXED_RATE}时，表示前一次作业调度下发后，隔多久触发第二次调度。
     */
    @Schema(title = "调度间隔时间",
            implementation = Float.class,
            description = "当调度方式为FIXED_DELAY时，表示前一次作业调度执行完成后，隔多久触发第二次调度。"
                    + "当调度方式为FIXED_RATE时，表示前一次作业调度下发后，隔多久触发第二次调度。"
    )
    private Duration scheduleInterval;

    /**
     * 作业调度的CRON表达式
     * 当调度方式为{@link ScheduleType#CRON}时，根据此CRON表达式计算得到的时间点触发作业调度。
     */
    @Schema(title = "作业调度的CRON表达式", description = "当调度方式为CRON时，根据此CRON表达式计算得到的时间点触发作业调度。")
    private String scheduleCron;

    /**
     * 作业调度的CRON表达式类型
     * 当调度方式为{@link ScheduleType#CRON}时，根据此CRON表达式计算得到的时间点触发作业调度。{@link com.cronutils.model.CronType}
     */
    @Schema(title = "作业调度的CRON表达式类型", description = "当调度方式为CRON时，根据此CRON表达式计算得到的时间点触发作业调度。")
    private String scheduleCronType = "QUARTZ";

}
