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

package org.limbo.flowjob.tracker.core.job.schedule;

import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleType;
import org.limbo.flowjob.tracker.commons.utils.strategies.Strategy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 作业调度时间计算策略，用于计算下次触发调度时间戳
 *
 * @author Brozen
 * @since 2021-05-20
 */
public abstract class JobScheduleCalculator implements Strategy<Job, Long> {

    /**
     * 作业没有下次触发时间时，返回0或负数
     */
    public static final long NO_TRIGGER = 0;

    /**
     * 此策略适用的作业调度类型
     */
    private final JobScheduleType scheduleType;

    protected JobScheduleCalculator(JobScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    /**
     * 此策略是否适用作业
     * @param job 作业
     * @return 是否可用此策略计算作业的触发时间
     */
    @Override
    public Boolean canApply(Job job) {
        return job.getScheduleOption().getScheduleType() == this.scheduleType;
    }

    /**
     * 通过此策略计算作业的下一次触发时间戳。如果不应该被触发，返回0或负数。
     * @param job 作业
     * @return 作业下次触发时间戳，当返回非正数时，表示作业不会有触发时间。
     */
    @Override
    public abstract Long apply(Job job);


    /**
     * 计算作业的开始调度时间，从作业创建时间开始，加上delay。
     * @param job 作业
     * @return 作业开始进行调度计算的时间
     */
    protected long calculateStartScheduleTimestamp(Job job) {
        LocalDateTime createdAt = job.getCreatedAt();
        Duration delay = job.getScheduleOption().getScheduleDelay();
        long startScheduleAt = createdAt.toEpochSecond(ZoneOffset.UTC);
        return delay != null ? startScheduleAt + delay.toMillis() : startScheduleAt;
    }

}
