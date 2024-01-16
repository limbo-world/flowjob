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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Brozen
 * @since 2022-10-11
 */
@Slf4j
public class MetaTaskScheduler extends HashedWheelTimerScheduler<MetaTask> {

    private final Map<String, MetaTask> scheduling;

    public MetaTaskScheduler(long tickDuration, TimeUnit unit) {
        super(tickDuration, unit);
        this.scheduling = new ConcurrentHashMap<>();
    }

    @Override
    public void schedule(MetaTask task) {
        String scheduleId = task.scheduleId();
        try {
            if (!task.verify()) {
                return;
            }
            // 存在就不需要重新放入
            if (scheduling.putIfAbsent(scheduleId, task) != null) {
                return;
            }

            calAndSchedule(task);
        } catch (Exception e) {
            log.error("Meta task [{}] execute failed", scheduleId, e);
        }
    }

    @Override
    public void unschedule(String id) {
        MetaTask task = scheduling.remove(id);
        if (task != null) {
            task.stop();
        }
    }

    @Override
    protected void afterExecute(MetaTask scheduled, Throwable thrown) {
        if (scheduled.isStopWhenError()) {
            unschedule(scheduled.scheduleId());
        }
    }

    public void reschedule(MetaTask task) {
        String scheduleId = task.scheduleId();
        try {
            calAndSchedule(task);
        } catch (Exception e) {
            log.error("Meta task [{}] reschedule failed", scheduleId, e);
        }
    }

}
