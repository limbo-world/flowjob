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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Brozen
 * @since 2022-10-11
 */
@Slf4j
public class MetaTaskScheduler extends HashedWheelTimerScheduler<MetaTask> {

    private final Map<MetaTaskType, Map<String, MetaTask>> scheduling;

    public MetaTaskScheduler() {
        super();
        this.scheduling = new EnumMap<>(MetaTaskType.class);
        for (MetaTaskType type : MetaTaskType.values()) {
            this.scheduling.put(type, new ConcurrentHashMap<>());
        }
    }

    @Override
    public void schedule(MetaTask task) {
        String scheduleId = task.scheduleId();
        try {
            if (isScheduling(scheduleId)) {
                return;
            }

            // 放入缓存
            scheduling.get(task.getType()).put(scheduleId, task);

            calAndSchedule(task);
        } catch (Exception e) {
            log.error("Meta task [{}] execute failed", scheduleId, e);
        }
    }

    @Override
    protected void afterExecute(MetaTask scheduled, Throwable t) {
        super.afterExecute(scheduled, t);
        if (t != null) {
            unschedule(scheduled.scheduleId());
        }
    }

    @Override
    public void unschedule(String id) {
        Map<String, MetaTask> metaTaskMap = scheduling.get(getType(id));
        metaTaskMap.remove(id);
    }

    @Override
    public boolean isScheduling(String id) {
        Map<String, MetaTask> metaTaskMap = scheduling.get(getType(id));
        return metaTaskMap.containsKey(id);
    }


    /**
     * org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask#scheduleId()
     */
    private MetaTaskType getType(String scheduleId) {
        String[] split = scheduleId.split("-");
        return MetaTaskType.parse(split[0]);
    }

    /**
     * 返回调度中的数据
     */
    public List<MetaTask> getSchedulingByType(MetaTaskType type) {
        return new ArrayList<>(scheduling.get(type).values());
    }

    public void reschedule(MetaTask task) {
        String scheduleId = task.scheduleId();
        try {
            if (!isScheduling(scheduleId)) {
                return;
            }
            calAndSchedule(task);
        } catch (Exception e) {
            log.error("Meta task [{}] reschedule failed", scheduleId, e);
        }
    }

}
