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

package org.limbo.flowjob.broker.application.plan.component;

import lombok.Setter;
import org.limbo.flowjob.broker.application.plan.service.PlanService;
import org.limbo.flowjob.broker.application.plan.support.EventListener;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventTopic;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.plan.ScheduleEventTopic;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Devil
 * @since 2022/8/5
 */
@Component
public class PlanInstanceScheduleListener implements EventListener {

    @Setter(onMethod_ = @Inject)
    private PlanService planService;

    @Override
    public EventTopic topic() {
        return ScheduleEventTopic.PLAN_EXECUTING;
    }

    @Override
    public void accept(Event event) {
        PlanInstance planInstance = (PlanInstance) event.getSource();
        planService.saveScheduleInfo(planInstance);
    }

}
