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

package org.limbo.flowjob.broker.core.meta.instance;

import org.limbo.flowjob.api.constants.InstanceStatus;
import org.limbo.flowjob.api.constants.InstanceType;
import org.limbo.flowjob.broker.core.meta.info.Plan;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2023/12/30
 */
public class InstanceFactory {

    public static PlanInstance create(String id, Plan plan, Attributes attributes, LocalDateTime triggerAt) {
        ScheduleOption scheduleOption = plan.getScheduleOption();
        return PlanInstance.builder()
                .id(id)
                .planId(plan.getId())
                .version(plan.getVersion())
                .status(InstanceStatus.DISPATCHING)
                .type(plan.getType())
                .triggerType(plan.getTriggerType())
                .scheduleType(scheduleOption.getScheduleType())
                .dag(plan.getDag())
                .attributes(attributes == null ? new Attributes() : attributes)
                .triggerAt(triggerAt)
                .build();
    }

    public static DelayInstance create(String id, String bizType, String bizId, InstanceType type, Attributes attributes, LocalDateTime triggerAt, DAG<WorkflowJobInfo> dag) {
        return DelayInstance.builder()
                .id(id)
                .bizType(bizType)
                .bizId(bizId)
                .triggerAt(triggerAt)
                .type(type)
                .attributes(attributes == null ? new Attributes() : attributes)
                .status(InstanceStatus.SCHEDULING)
                .dag(dag)
                .build();
    }
}
