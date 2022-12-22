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

import java.util.Collection;
import java.util.EnumMap;
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
        try {
            if (isScheduling(task.scheduleId())) {
                return;
            }

            // 放入缓存
            scheduling.get(task.getType()).put(task.scheduleId(), task);

            calAndSchedule(task);
        } catch (Exception e) {
            log.error("Meta task [{}] execute failed", task.scheduleId(), e);
        }
    }

    @Override
    protected void afterExecute(MetaTask task) {
        unschedule(task.scheduleId());
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

    private MetaTaskType getType(String scheduleId) {
        String[] split = scheduleId.split("-");
        return MetaTaskType.parse(split[0]);
    }

    /**
     * 返回调度中的数据
     */
    public Collection<MetaTask> getSchedulingByType(MetaTaskType type) {
        return scheduling.get(type).values();
    }

}
