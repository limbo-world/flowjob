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

package org.limbo.flowjob.tracker.core.schedule;

import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.broker.core.utils.strategies.Strategy;
import org.limbo.flowjob.tracker.core.plan.ScheduleOption;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 调度时间计算策略，用于计算下次触发调度时间戳
 *
 * @author Brozen
 * @since 2021-05-20
 */
public abstract class ScheduleCalculator implements Strategy<Schedulable, Long> {

    /**
     * 没有下次触发时间时，返回0或负数
     */
    public static final long NO_TRIGGER = 0;

    /**
     * 此策略适用的调度类型
     */
    private final ScheduleType scheduleType;

    protected ScheduleCalculator(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }


    /**
     * 此策略是否适用于待调度对象
     * @param schedulable 可调度的对象
     * @return 是否可用此策略计算作业的触发时间
     */
    @Override
    public Boolean canApply(Schedulable schedulable) {
        return schedulable.getScheduleOption().getScheduleType() == this.scheduleType;
    }


    /**
     * 通过此策略计算下一次触发调度的时间戳。如果不应该被触发，返回0或负数。
     * @param schedulable 待调度对象
     * @return 下次触发调度的时间戳，当返回非正数时，表示作业不会有触发时间。
     */
    @Override
    public abstract Long apply(Schedulable schedulable);


    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    /**
     * 计算作业的开始调度时间，从作业创建时间开始，加上delay。
     * @param scheduleOption 作业调度配置
     * @return 作业开始进行调度计算的时间
     */
    protected long calculateStartScheduleTimestamp(ScheduleOption scheduleOption) {
        LocalDateTime startAt = scheduleOption.getScheduleStartAt();
        Duration delay = scheduleOption.getScheduleDelay();
        long startScheduleAt = startAt.toEpochSecond(TimeUtil.zoneOffset());
        return delay != null ? startScheduleAt + delay.toMillis() : startScheduleAt;
    }

}
