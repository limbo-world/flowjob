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

package org.limbo.flowjob.broker.core.schedule.scheduler;

import org.limbo.flowjob.broker.core.schedule.Schedulable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Devil
 * @since 2022/7/25
 */
public class CacheableScheduler implements Scheduler {

    /**
     * 所有正在被调度中的对象
     */
    private final Map<String, Schedulable> scheduling;

    public CacheableScheduler() {
        this.scheduling = new ConcurrentHashMap<>();
    }

    @Override
    public void schedule(Schedulable schedulable) {
        if (checkAndPut(schedulable)) {
            schedulable.schedule();
        }
    }

    /**
     * 是否进行了缓存
     * @return 放入返回true 否则返回false
     */
    public boolean checkAndPut(Schedulable schedulable) {
        String id = schedulable.scheduleId();
        if (scheduling.containsKey(id)) {
            scheduling.put(id, schedulable);
            return true;
        }
        return false;
    }

    @Override
    public void unschedule(String id) {
        scheduling.remove(id);
    }

    @Override
    public boolean isScheduling(String id) {
        return scheduling.containsKey(id);
    }

}
