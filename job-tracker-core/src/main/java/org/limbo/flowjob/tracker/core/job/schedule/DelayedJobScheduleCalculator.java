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
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.commons.utils.strategies.Strategy;
import org.limbo.flowjob.tracker.core.job.JobScheduleOption;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleType;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 固定延迟作业调度时间计算器
 *
 * @author Brozen
 * @since 2021-05-21
 */
public class DelayedJobScheduleCalculator extends JobScheduleCalculator implements Strategy<Job, Long> {

    /**
     * 作业上下文repository
     */
    private JobContextRepository jobContextRepository;

    protected DelayedJobScheduleCalculator(JobContextRepository jobContextRepository) {
        super(JobScheduleType.DELAYED);
        this.jobContextRepository = jobContextRepository;
    }

    /**
     * 通过此策略计算作业的下一次触发时间戳
     * @param job 作业
     * @return 作业下次触发时间戳
     */
    @Override
    public Long apply(Job job) {
        // 只调度一次
        JobContext latestContext = jobContextRepository.getLatestContext(job.getJobId());
        if (latestContext != null) {
            return NO_TRIGGER;
        }

        // 从创建时间开始，间隔固定delay进行调度
        JobScheduleOption scheduleOption = job.getScheduleOption();
        LocalDateTime startAt = scheduleOption.getScheduleStartAt();
        Duration delay = scheduleOption.getScheduleDelay();
        long triggerAt = startAt.toEpochSecond(ZoneOffset.UTC);
        triggerAt = delay != null ? triggerAt + delay.toMillis() : triggerAt;

        long now = Instant.now().getEpochSecond();
        return Math.max(triggerAt, now);
    }

}
