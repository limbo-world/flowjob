/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.schedule;

import java.time.LocalDateTime;

/**
 * 待调度对象接口
 *
 * @author Brozen
 * @since 2021-07-12
 */
public interface Scheduled {

    /**
     * 获取调度对象ID
     */
    String scheduleId();

    /**
     * 任务执行
     */
    void execute();

    /**
     * 任务应该被执行的时间
     */
    LocalDateTime scheduleAt();

}
