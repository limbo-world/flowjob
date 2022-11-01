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

package org.limbo.flowjob.broker.core.schedule.calculator;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.broker.core.schedule.Calculated;
import org.limbo.flowjob.broker.core.schedule.Scheduled;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.utils.TimeUtil;

import java.time.Duration;

/**
 * 固定速度作业调度时间计算器
 *
 * @author Brozen
 * @since 2021-05-21
 */
@Slf4j
public class FixRateScheduleCalculator extends ScheduleCalculator {

    protected FixRateScheduleCalculator() {
        super(ScheduleType.FIXED_RATE);
    }


    /**
     * 通过此策略计算下一次触发调度的时间戳。如果不应该被触发，返回0或负数。
     *
     * @param calculated 待调度对象
     * @return 下次触发调度的时间戳，当返回非正数时，表示作业不会有触发时间。
     */
    @Override
    public Long calculate(Calculated calculated) {

        ScheduleOption scheduleOption = calculated.scheduleOption();
        long now = TimeUtil.currentInstant().getEpochSecond();
        long startScheduleAt = calculateStartScheduleTimestamp(scheduleOption);

        // 计算第一次调度
        if (calculated.lastScheduleAt() == null) {
            return Math.max(startScheduleAt, now);
        }

        // 上次调度一定间隔后调度
        Duration interval = scheduleOption.getScheduleInterval();
        if (interval == null) {
            log.error("cannot calculate next trigger timestamp of {} because interval is not assigned!", calculated);
            return ScheduleCalculator.NO_TRIGGER;
        }

        long scheduleAt = TimeUtil.toInstant(calculated.lastScheduleAt()).toEpochMilli() + interval.toMillis();
        return Math.max(scheduleAt, now);
    }


}
