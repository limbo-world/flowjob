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

package org.limbo.flowjob.broker.core.domain.plan;

import lombok.Getter;
import lombok.ToString;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.TriggerType;

/**
 * 执行计划。一个计划{@link Plan}对应至少一个作业{@link WorkflowJobInfo}
 * 主要是对plan的管理
 *
 * @author Brozen
 * @since 2021-07-12
 */
@Getter
@ToString
public abstract class Plan {

    private static final long serialVersionUID = 5657376836197403211L;

    /**
     * 作业执行计划ID
     */
    private final String planId;

    /**
     * 版本
     */
    private final String version;

    /**
     * 类型
     */
    private final PlanType type;

    /**
     * 触发类型
     */
    private final TriggerType triggerType;

    /**
     * 作业计划调度配置参数
     */
    private final ScheduleOption scheduleOption;

    protected Plan(String planId, String version, PlanType type, TriggerType triggerType,
                   ScheduleOption scheduleOption) {
        this.planId = planId;
        this.version = version;
        this.type = type;
        this.triggerType = triggerType;
        this.scheduleOption = scheduleOption;
    }

    public abstract PlanType planType();



}
