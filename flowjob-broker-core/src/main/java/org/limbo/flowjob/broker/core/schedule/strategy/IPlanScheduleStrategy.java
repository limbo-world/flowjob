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

import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.common.constants.TriggerType;

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2023/2/8
 */
public interface IPlanScheduleStrategy {

    /**
     * 创建plan和相关job等信息
     * @param triggerType PlanScheduleTask 为调度触发 还可能是api触发执行
     * @param plan
     * @param triggerAt
     */
    void schedule(TriggerType triggerType, Plan plan, LocalDateTime triggerAt);

}
