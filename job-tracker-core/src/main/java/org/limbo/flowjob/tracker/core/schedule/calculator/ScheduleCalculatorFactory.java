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

import org.limbo.flowjob.tracker.commons.utils.strategies.StrategyFactory;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.ScheduleCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Brozen
 * @since 2021-05-20
 */
public class ScheduleCalculatorFactory implements StrategyFactory<Schedulable<?>, ScheduleCalculator, Schedulable<?>, Long> {

    /**
     * 全部策略
     */
    private List<ScheduleCalculator> scheduleCalculators;

    public ScheduleCalculatorFactory() {
        ArrayList<ScheduleCalculator> calculators = new ArrayList<>();

        // 预设计算器
        calculators.add(new DelayedScheduleCalculator());
        calculators.add(new FixRateScheduleCalculator());
        calculators.add(new FixIntervalScheduleCalculator());
        calculators.add(new CronScheduleCalculator());

        // 自定义计算器

        // 配置不可变
        scheduleCalculators = Collections.unmodifiableList(calculators);

    }


    /**
     * 根据作业调度类型，创建作业触发时间计算器
     * @param schedulable 待调度对象
     * @return 触发时间计算器
     */
    @Override
    public ScheduleCalculator newStrategy(Schedulable<?> schedulable) {
        for (ScheduleCalculator calculator : scheduleCalculators) {
            if (calculator.canApply(schedulable)) {
                return calculator;
            }
        }

        throw new IllegalStateException("cannot apply " + (getClass().getName()) + " for " + schedulable);
    }

}
