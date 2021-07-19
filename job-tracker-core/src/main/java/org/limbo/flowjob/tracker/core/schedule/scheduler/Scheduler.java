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

package org.limbo.flowjob.tracker.core.schedule.scheduler;

import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.SchedulableInstance;

/**
 * 调度器，封装了调度流程，根据{@link ScheduleType}有不同实现。
 *
 * @author Brozen
 * @since 2021-05-18
 */
public interface Scheduler<T extends SchedulableInstance> {

    /**
     * 开始调度
     * @param schedulable 待调度的对象
     */
    void schedule(Schedulable<T> schedulable);

    /**
     * 停止调度
     * @param id 待调度的对象 id
     */
    void unschedule(String id);

}
