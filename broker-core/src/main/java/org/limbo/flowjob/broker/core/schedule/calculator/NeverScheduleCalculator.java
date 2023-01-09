/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.schedule.calculator;

import org.limbo.flowjob.broker.core.schedule.Calculated;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.common.constants.ScheduleType;

/**
 * @author Brozen
 * @since 2022-12-22
 */
public class NeverScheduleCalculator extends ScheduleCalculator {

    public NeverScheduleCalculator() {
        super(ScheduleType.NONE);
    }

    /**
     * {@inheritDoc}
     * @param calculated
     * @return
     */
    @Override
    public Long doCalculate(Calculated calculated) {
        return Long.MAX_VALUE;
    }

}
