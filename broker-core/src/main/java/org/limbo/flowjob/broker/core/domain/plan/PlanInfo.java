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
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.TriggerType;

import java.io.Serializable;

/**
 * @author Brozen
 * @since 2021-10-14
 */
@Getter
@ToString
public abstract class PlanInfo implements Serializable {

    private static final long serialVersionUID = -3488415933872953356L;

    /**
     * 作业执行计划ID
     */
    private final String planId;

    /**
     * 版本
     */
    private final String version;

    /**
     * 触发类型
     */
    private final TriggerType triggerType;

    /**
     * 作业计划调度配置参数
     */
    private final ScheduleOption scheduleOption;

    protected PlanInfo(String planId, String version, TriggerType triggerType,
                    ScheduleOption scheduleOption) {
        this.planId = planId;
        this.version = version;
        this.triggerType = triggerType;
        this.scheduleOption = scheduleOption;
    }

    public abstract PlanType planType();

}
