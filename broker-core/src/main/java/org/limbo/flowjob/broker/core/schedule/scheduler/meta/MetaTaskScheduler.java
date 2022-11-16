/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.schedule.scheduler.meta;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.schedule.scheduler.HashedWheelTimerScheduler;

/**
 * @author Brozen
 * @since 2022-10-11
 */
@Slf4j
public class MetaTaskScheduler extends HashedWheelTimerScheduler<MetaTask> {


    /**
     * 执行元任务
     */
    @Override
    protected void doSchedule(MetaTask task) {
        try {
            task.execute();
        } catch (Exception e) {
            log.error("Meta task [{}] execute failed", task.scheduleId(), e);
        }
    }


}
