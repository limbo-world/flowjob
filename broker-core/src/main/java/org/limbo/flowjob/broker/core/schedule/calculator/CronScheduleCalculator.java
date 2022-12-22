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

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.broker.core.schedule.Calculated;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * CRON调度时间计算器
 *
 * @author Brozen
 * @since 2021-05-21
 */
@Slf4j
public class CronScheduleCalculator extends ScheduleCalculator {

    protected CronScheduleCalculator() {
        super(ScheduleType.CRON);
    }

    /**
     * 通过此策略计算下一次触发调度的时间戳。如果不应该被触发，返回0或负数。
     * @param calculated 待调度对象
     * @return 下次触发调度的时间戳，当返回非正数时，表示作业不会有触发时间。
     */
    @Override
    public Long doCalculate(Calculated calculated) {
        ScheduleOption scheduleOption = calculated.scheduleOption();
        // 计算下一次调度
        String cron = scheduleOption.getScheduleCron();
        String cronType = scheduleOption.getScheduleCronType();
        try {
            // 校验CRON表达式
            CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.valueOf(cronType));
            CronParser parser = new CronParser(cronDefinition);
            ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(cron));

            // 解析下次触发时间
            Optional<ZonedDateTime> nextSchedule = executionTime.nextExecution(ZonedDateTime.now());
            if (!nextSchedule.isPresent()) {
                log.error("cron expression {} {} next schedule is null", cron, cronType);
                return ScheduleCalculator.NO_TRIGGER;
            }
            return nextSchedule.get().toInstant().toEpochMilli();
        } catch (Exception e) {
            log.error("parse cron expression {} {} failed!", cron, cronType, e);
            return ScheduleCalculator.NO_TRIGGER;
        }

    }

}
