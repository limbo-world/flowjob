/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.core.schedule.strategy;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.common.constants.PlanType;

import java.util.Map;

/**
 * @author Devil
 * @since 2022/12/19
 */
@Slf4j
public class ScheduleStrategyFactory {

    private final Map<PlanType, IScheduleStrategy> strategyMap;

    public ScheduleStrategyFactory(Map<PlanType, IScheduleStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }

    public IScheduleStrategy build(PlanType planType) {
        return strategyMap.get(planType);
    }

}
