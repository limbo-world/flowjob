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

import org.limbo.flowjob.broker.core.schedule.Scheduled;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供缓存的调度器
 *
 * @author Devil
 * @since 2022/7/25
 */
public abstract class CacheableScheduler<T extends Scheduled> implements Scheduler<T> {

    /**
     * 所有正在被调度中的对象
     */
    private final Map<String, Scheduled> scheduling;

    public CacheableScheduler() {
        this.scheduling = new ConcurrentHashMap<>();
    }

    /**
     * 是否进行了缓存
     * @return 放入返回true 否则返回false
     */
    public Scheduled put(Scheduled scheduled) {
        return scheduling.put(scheduled.scheduleId(), scheduled);
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
