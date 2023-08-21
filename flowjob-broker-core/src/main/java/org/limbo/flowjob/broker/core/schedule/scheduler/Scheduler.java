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

package org.limbo.flowjob.broker.core.schedule.scheduler;

import org.limbo.flowjob.api.constants.ScheduleType;
import org.limbo.flowjob.broker.core.schedule.Scheduled;

/**
 * 调度器，封装了调度流程，根据{@link ScheduleType}有不同实现。
 *
 * @author Brozen
 * @since 2021-05-18
 */
public interface Scheduler<T extends Scheduled> {

    /**
     * 进行一次调度 如果任务已存在 不会重复调度
     * @param scheduled 待调度的对象
     */
    void schedule(T scheduled);

    /**
     * 停止调度
     * @param id 待调度的对象 id
     */
    void unschedule(String id);

}
