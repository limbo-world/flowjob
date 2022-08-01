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

import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.core.schedule.DelegatedScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Brozen
 * @since 2021-05-20
 */
public class SimpleScheduleCalculatorFactory implements ScheduleCalculatorFactory {

    /**
     * 全部策略
     */
    private final Map<ScheduleType, ScheduleCalculator> scheduleCalculators;

    public SimpleScheduleCalculatorFactory() {
        Map<ScheduleType, ScheduleCalculator> calculators = new EnumMap<>(ScheduleType.class);

        // 预设计算器
        putCalculator(calculators, new FixRateScheduleCalculator());
        putCalculator(calculators, new FixDelayScheduleCalculator());
        putCalculator(calculators, new CronScheduleCalculator());

        // 自定义计算器

        // 配置不可变
        scheduleCalculators = Collections.unmodifiableMap(calculators);

    }

    private void putCalculator(Map<ScheduleType, ScheduleCalculator> calculators, ScheduleCalculator calculator) {
        calculators.put(calculator.getScheduleType(), calculator);
    }


    /**
     * 根据作业调度类型，创建作业触发时间计算器
     * @param scheduleType 调度方式
     * @return 触发时间计算器
     */
    @Override
    public ScheduleCalculator apply(ScheduleType scheduleType) {
        ScheduleCalculator calculator = scheduleCalculators.get(scheduleType);
        if (calculator != null) {
            return new DelegatedScheduleCalculator(calculator);
        }

        throw new IllegalStateException("cannot apply " + (getClass().getName()) + " for " + scheduleType);
    }
}
