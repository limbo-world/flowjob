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

package org.limbo.flowjob.broker.core.schedule;

import lombok.Setter;

import java.util.Objects;

/**
 * 代理功能 可以基于此类进行扩展
 *
 * @author Brozen
 * @since 2021-07-13
 */
public class DelegatedScheduleCalculator extends ScheduleCalculator {

    /**
     * 被代理的调度时间计算器
     */
    @Setter
    private ScheduleCalculator delegated;


    public DelegatedScheduleCalculator(ScheduleCalculator delegated) {
        super(delegated.getScheduleType());
        this.delegated = delegated;
    }

    /**
     * {@inheritDoc}
     * @param calculated 可调度的对象
     * @return
     */
    @Override
    public Boolean canCalculate(Calculated calculated) {
        return Objects.requireNonNull(delegated, "delegated target").canCalculate(calculated);
    }


    /**
     * {@inheritDoc}
     * @param calculated 待调度对象
     * @return
     */
    @Override
    public Long calculate(Calculated calculated) {
        return Objects.requireNonNull(delegated, "delegated target").calculate(calculated);
    }
}
