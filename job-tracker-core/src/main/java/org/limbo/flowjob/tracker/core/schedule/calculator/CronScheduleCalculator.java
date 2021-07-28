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

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.utils.strategies.Strategy;
import org.limbo.flowjob.tracker.core.plan.ScheduleOption;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.ScheduleCalculator;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

/**
 * CRON调度时间计算器
 *
 * @author Brozen
 * @since 2021-05-21
 */
@Slf4j
public class CronScheduleCalculator extends ScheduleCalculator implements Strategy<Schedulable, Long> {

    protected CronScheduleCalculator() {
        super(ScheduleType.CRON);
    }


    /**
     * 通过此策略计算下一次触发调度的时间戳。如果不应该被触发，返回0或负数。
     * @param schedulable 待调度对象
     * @return 下次触发调度的时间戳，当返回非正数时，表示作业不会有触发时间。
     */
    @Override
    public Long apply(Schedulable schedulable) {

        // 未到调度开始时间，不触发下次调度
        Instant nowInstant = Instant.now();
        ScheduleOption scheduleOption = schedulable.getScheduleOption();
        long startScheduleAt = calculateStartScheduleTimestamp(scheduleOption);
        if (nowInstant.getEpochSecond() < startScheduleAt) {
            return NO_TRIGGER;
        }

        String cron = scheduleOption.getScheduleCron();
        try {
            // 校验CRON表达式
            CronExpression.validateExpression(cron);
            CronExpression expression = new CronExpression(cron);

            // 解析下次触发时间
            Date nextSchedule = expression.getNextValidTimeAfter(Date.from(nowInstant));
            if (nextSchedule == null) {
                return NO_TRIGGER;
            }

            return nextSchedule.getTime();
        } catch (ParseException e) {
            log.error("parse cron expression {} failed!", cron, e);
            return NO_TRIGGER;
        }

    }

}
