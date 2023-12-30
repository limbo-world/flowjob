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

package org.limbo.flowjob.broker.core.meta.plan;

import org.limbo.flowjob.api.constants.ConstantsPool;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2023/12/30
 */
public class PlanInstanceFactory {

    public static PlanInstance create(String id, Plan plan, Attributes planAttributes, LocalDateTime triggerAt) {
        return PlanInstance.builder()
                .id(id)
                .planId(plan.getId())
                .version(plan.getVersion())
                .status(ConstantsPool.PLAN_DISPATCHING)
                .type(plan.getType())
                .triggerType(plan.getTriggerType())
                .scheduleOption(plan.getScheduleOption())
                .dag(plan.getDag())
                .attributes(planAttributes == null ? new Attributes() : planAttributes)
                .triggerAt(triggerAt)
                .build();
    }
}
