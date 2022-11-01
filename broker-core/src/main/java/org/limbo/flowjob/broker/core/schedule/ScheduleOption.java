/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.schedule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TriggerType;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 作业调度配置，值对象。
 * PS: 新版JDK出来就可以用Record了
 *
 * @author Brozen
 * @since 2021-06-01
 */
@Getter
@Setter(AccessLevel.NONE)
public class ScheduleOption implements Serializable {

    private static final long serialVersionUID = -7313895910957594962L;

    /**
     * 调度方式
     */
    private final ScheduleType scheduleType;

    /**
     * 触发类型
     */
    private final TriggerType triggerType;

    /**
     * 调度开始时间，从此时间开始执行调度。
     */
    private final LocalDateTime scheduleStartAt;

    /**
     * 延迟时间 -- 当前时间多久后调度
     */
    private final Duration scheduleDelay;

    /**
     * 获取调度间隔时间。
     * 当调度方式为{@link ScheduleType#FIXED_DELAY}时，表示前一次作业调度执行完成后，隔多久触发第二次调度。
     * 当调度方式为{@link ScheduleType#FIXED_RATE}时，表示前一次作业调度下发后，隔多久触发第二次调度。
     */
    private final Duration scheduleInterval;

    /**
     * 作业调度的CRON表达式
     * 当调度方式为{@link ScheduleType#CRON}时，根据此CRON表达式计算得到的时间点触发作业调度。
     */
    private final String scheduleCron;

    /**
     * 作业调度的CRON表达式的类型
     * 当调度方式为{@link ScheduleType#CRON}时，根据此CRON表达式类型计算得到的时间点触发作业调度。{@link com.cronutils.model.CronType}
     */
    private final String scheduleCronType;

//    @JsonCreator // 加上@JsonProperty("scheduleType") 不去掉mapstruct会用set方式，比较奇怪
    public ScheduleOption(ScheduleType scheduleType,
                          TriggerType triggerType,
                          LocalDateTime scheduleStartAt,
                          Duration scheduleDelay,
                          Duration scheduleInterval,
                          String scheduleCron,
                          String scheduleCronType) {
        this.scheduleType = scheduleType;
        this.triggerType = triggerType;
        this.scheduleStartAt = scheduleStartAt;
        this.scheduleDelay = scheduleDelay == null ? Duration.ZERO : scheduleDelay;
        this.scheduleInterval = scheduleInterval == null ? Duration.ZERO : scheduleInterval;
        this.scheduleCron = scheduleCron;
        this.scheduleCronType = scheduleCronType;
    }

}
