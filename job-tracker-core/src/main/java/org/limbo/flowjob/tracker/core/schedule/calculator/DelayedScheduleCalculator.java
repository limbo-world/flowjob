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

package org.limbo.flowjob.tracker.core.schedule.calculator;

import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.utils.strategies.Strategy;
import org.limbo.flowjob.tracker.core.plan.ScheduleOption;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.ScheduleCalculator;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 固定延迟作业调度时间计算器
 *
 * @author Brozen
 * @since 2021-05-21
 */
public class DelayedScheduleCalculator extends ScheduleCalculator implements Strategy<Schedulable, Long> {


    protected DelayedScheduleCalculator() {
        super(ScheduleType.DELAYED);
    }


    /**
     * 通过此策略计算下一次触发调度的时间戳。如果不应该被触发，返回0或负数。
     * @param schedulable 待调度对象
     * @return 下次触发调度的时间戳，当返回非正数时，表示作业不会有触发时间。
     */
    @Override
    public Long apply(Schedulable schedulable) {
        // 只调度一次
        Instant lastScheduleAt = schedulable.getLastScheduleAt();
        if (lastScheduleAt != null) {
            return NO_TRIGGER;
        }

        // 从创建时间开始，间隔固定delay进行调度
        ScheduleOption scheduleOption = schedulable.getScheduleOption();
        LocalDateTime startAt = scheduleOption.getScheduleStartAt();
        Duration delay = scheduleOption.getScheduleDelay();
        long triggerAt = startAt.toEpochSecond(ZoneOffset.UTC);
        triggerAt = delay != null ? triggerAt + delay.toMillis() : triggerAt;

        long now = Instant.now().getEpochSecond();
        return Math.max(triggerAt, now);
    }

}
