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

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2022-10-11
 */
public abstract class FixIntervalMetaTask extends MetaTask {

    /**
     * 首次触发任务时的延迟时间，默认 0s。
     */
    private final Duration delay;

    /**
     * 两次任务触发的间隔。
     */
    private final Duration interval;


    protected FixIntervalMetaTask(String taskId, Duration interval) {
        this(taskId, Duration.ZERO, interval);
    }


    protected FixIntervalMetaTask(String taskId, Duration delay, Duration interval) {
        super(taskId);
        this.delay = delay;
        this.interval = interval;
    }

    /**
     * {@inheritDoc}
     *
     * @param firstTime 是否计算首次触发间隔
     * @return
     */
    @Override
    protected LocalDateTime calculateNextTriggerTime(boolean firstTime) {
        if (firstTime) {
            return LocalDateTime.now().plusSeconds(delay.getSeconds());
        } else {
            return LocalDateTime.now().plusSeconds(interval.getSeconds());
        }
    }

}
