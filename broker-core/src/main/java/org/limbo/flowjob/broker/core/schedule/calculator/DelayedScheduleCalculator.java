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
import org.limbo.flowjob.broker.core.schedule.Schedulable;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.core.utils.strategies.Strategy;

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
        // 从创建时间开始，间隔固定delay调度
        long startScheduleAt = calculateStartScheduleTimestamp(schedulable.getScheduleOption());
        long now = TimeUtil.nowInstant().getEpochSecond();
        return Math.max(startScheduleAt, now);
    }

}
