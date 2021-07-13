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

import lombok.Setter;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;

import java.util.Objects;

/**
 * @author Brozen
 * @since 2021-07-13
 */
public class DelegatedScheduleCalculator extends ScheduleCalculator {

    /**
     * 被代理的调度时间计算器
     */
    @Setter
    private ScheduleCalculator delegated;


    public DelegatedScheduleCalculator(ScheduleType scheduleType) {
        super(scheduleType);
    }

    /**
     * {@inheritDoc}
     * @param schedulable 可调度的对象
     * @return
     */
    @Override
    public Boolean canApply(Schedulable<?> schedulable) {
        return Objects.requireNonNull(delegated, "delegated target").canApply(schedulable);
    }


    /**
     * {@inheritDoc}
     * @param schedulable 待调度对象
     * @return
     */
    @Override
    public Long apply(Schedulable<?> schedulable) {
        return Objects.requireNonNull(delegated, "delegated target").apply(schedulable);
    }
}
