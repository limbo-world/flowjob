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

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2023/12/13
 */
public interface PlanInstanceRepository {

    PlanInstance get(String id);

    void save(PlanInstance planInstance);

    boolean executing(String planInstanceId, LocalDateTime startAt);

    boolean success(String planInstanceId, LocalDateTime feedbackAt);

    boolean fail(String planInstanceId, LocalDateTime startAt, LocalDateTime feedbackAt);
}
