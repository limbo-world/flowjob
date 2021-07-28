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

package org.limbo.flowjob.tracker.core.plan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.dto.plan.ScheduleOptionDto;

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
public class ScheduleOption {

    /**
     * 作业调度方式
     */
    private final ScheduleType scheduleType;

    /**
     * 作业调度开始时间，从此时间开始执行调度。
     */
    private final LocalDateTime scheduleStartAt;

    /**
     * 获取作业延迟时间。
     * 当调度方式为{@link ScheduleType#DELAYED}或{@link ScheduleType#FIXED_INTERVAL}时，
     * 表示从<code>scheduleStartAt</code>时间点开始，延迟多久触发作业调度。
     */
    private final Duration scheduleDelay;

    /**
     * 获取作业调度间隔时间。
     * 当调度方式为{@link ScheduleType#FIXED_INTERVAL}时，表示前一次作业调度执行完成后，隔多久触发第二次调度。
     * 当调度方式为{@link ScheduleType#FIXED_RATE}时，表示前一次作业调度下发后，隔多久触发第二次调度。
     */
    private final Duration scheduleInterval;

    /**
     * 作业调度的CRON表达式
     * 当调度方式为{@link ScheduleType#CRON}时，根据此CRON表达式计算得到的时间点触发作业调度。
     */
    private final String scheduleCron;

    @JsonCreator
    public ScheduleOption(
            @JsonProperty("scheduleType") ScheduleType scheduleType,
            @JsonProperty("scheduleStartAt") LocalDateTime scheduleStartAt,
            @JsonProperty("scheduleDelay") Duration scheduleDelay,
            @JsonProperty("scheduleInterval") Duration scheduleInterval,
            @JsonProperty("scheduleCron") String scheduleCron) {
        this.scheduleType = scheduleType;
        this.scheduleStartAt = scheduleStartAt;
        this.scheduleDelay = scheduleDelay == null ? Duration.ZERO : scheduleDelay;
        this.scheduleInterval = scheduleInterval == null ? Duration.ZERO : scheduleInterval;
        this.scheduleCron = scheduleCron;
    }


    /**
     * 将DTO中的调度配置合并到此调度配置中，并生成新的调度配置对象
     * @param other DTO入参
     * @return 新的调度配置信息
     */
    public ScheduleOption mergeIntoCurrent(ScheduleOptionDto other) {
        return new ScheduleOption(
                other.getScheduleType() != null ? other.getScheduleType() : this.getScheduleType(),
                other.getScheduleStartAt() != null ? other.getScheduleStartAt() : this.getScheduleStartAt(),
                other.getScheduleDelay() != null ? other.getScheduleDelay() : this.getScheduleDelay(),
                other.getScheduleInterval() != null ? other.getScheduleInterval() : this.getScheduleInterval(),
                other.getScheduleCron() != null ? other.getScheduleCron() : this.getScheduleCron()
        );
    }

}
