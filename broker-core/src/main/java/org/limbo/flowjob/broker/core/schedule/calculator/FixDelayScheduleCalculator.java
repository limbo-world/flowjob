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
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.core.schedule.Schedulable;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.core.utils.strategies.Strategy;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 固定间隔作业调度时间计算器
 *
 * @author Brozen
 * @since 2021-05-21
 */
@Slf4j
public class FixDelayScheduleCalculator extends ScheduleCalculator implements Strategy<Schedulable, Long> {

    protected FixDelayScheduleCalculator() {
        super(ScheduleType.FIXED_DELAY);
    }

    /**
     * 通过此策略计算下一次触发调度的时间戳。如果不应该被触发，返回0或负数。
     *
     * @param schedulable 待调度对象
     * @return 下次触发调度的时间戳，当返回非正数时，表示作业不会有触发时间。
     */
    @Override
    public Long apply(Schedulable schedulable) {

        ScheduleOption scheduleOption = schedulable.scheduleOption();
        long now = TimeUtil.nowInstant().getEpochSecond();
        long startScheduleAt = calculateStartScheduleTimestamp(scheduleOption);

        // 计算第一次调度
        if (schedulable.scheduleAt() == null) {
            return Math.max(startScheduleAt, now);
        }

        LocalDateTime lastFeedbackAt = schedulable.feedbackAt();
        // 如果为空，表示此次上次任务还没反馈，等待反馈后重新调度
        if (lastFeedbackAt == null) {
            return ScheduleCalculator.NO_TRIGGER;
        }

        Duration interval = scheduleOption.getScheduleInterval();
        if (interval == null) {
            log.error("cannot calculate next trigger timestamp of {} because interval is not assigned!", schedulable);
            return ScheduleCalculator.NO_TRIGGER;
        }

        long scheduleAt = TimeUtil.toInstant(lastFeedbackAt).toEpochMilli() + interval.toMillis();
        return Math.max(scheduleAt, now);
    }

}
