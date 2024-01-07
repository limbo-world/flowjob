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

import lombok.Builder;
import lombok.Getter;
import org.limbo.flowjob.api.constants.InstanceStatus;
import org.limbo.flowjob.api.constants.InstanceType;
import org.limbo.flowjob.api.constants.ScheduleType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2023/12/13
 */
@Getter
public class PlanInstance extends Instance {

    /**
     * 作业执行计划ID
     */
    private String planId;

    /**
     * 版本
     */
    private String version;

    /**
     * 触发类型
     */
    private TriggerType triggerType;

    /**
     * 作业计划调度配置参数
     */
    private ScheduleType scheduleType;

    @Builder
    public PlanInstance(String id, InstanceType type, InstanceStatus status, Attributes attributes, LocalDateTime triggerAt, LocalDateTime startAt, LocalDateTime feedbackAt, String planId, String version, DAG<WorkflowJobInfo> dag, TriggerType triggerType, ScheduleType scheduleType) {
        super(id, type, status, dag, attributes, triggerAt, startAt, feedbackAt);
        this.planId = planId;
        this.version = version;
        this.triggerType = triggerType;
        this.scheduleType = scheduleType;
    }
}
