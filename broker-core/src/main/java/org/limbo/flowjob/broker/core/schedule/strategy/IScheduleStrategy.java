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
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.common.constants.TriggerType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Devil
 * @since 2022/12/19
 */
public interface IScheduleStrategy {

    void schedule(TriggerType triggerType, Plan plan, LocalDateTime triggerAt);

    void schedule(Task task);

    void handleTaskSuccess(Task task, Map<String, Object> context, Object result);

    void handleTaskFail(Task task, String errorMsg, String errorStackTrace);

}
