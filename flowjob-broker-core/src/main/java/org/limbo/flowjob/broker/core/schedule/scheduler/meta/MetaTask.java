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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.broker.core.schedule.Scheduled;

/**
 * @author Brozen
 * @since 2022-10-11
 */
public abstract class MetaTask implements Scheduled {

    /**
     * 是否停止
     */
    private volatile boolean stopped = false;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    protected MetaTaskScheduler metaTaskScheduler;

    public MetaTask(MetaTaskScheduler metaTaskScheduler) {
        this.metaTaskScheduler = metaTaskScheduler;
    }

    @Override
    public void afterExecute(Throwable thrown) {
        metaTaskScheduler.unschedule(scheduleId());
    }

    @Override
    public String scheduleId() {
        return getType() + "-" + getMetaId();
    }

    /**
     * @return 任务类型
     */
    public abstract String getType();

    /**
     * @return 任务id
     */
    public abstract String getMetaId();

    @Override
    public void stop() {
        stopped = true;
    }

    @Override
    public boolean stopped() {
        return stopped;
    }


}
