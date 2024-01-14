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

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2024/1/4
 */
public interface DelayInstanceRepository {

    DelayInstance get(String bizType, String bizId);

    DelayInstance get(String id);

    DelayInstance lockAndGet(String id);

    void save(DelayInstance instance);

    boolean executing(String instanceId, LocalDateTime startAt);

    boolean success(String instanceId, LocalDateTime feedbackAt);

    boolean fail(String instanceId, LocalDateTime startAt, LocalDateTime feedbackAt);
}
