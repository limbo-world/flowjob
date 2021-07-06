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

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.commons.utils.strategies.Strategy;
import org.limbo.flowjob.tracker.core.job.JobScheduleOption;
import org.limbo.flowjob.tracker.core.job.context.JobContextRepository;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleType;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * 固定速度作业调度时间计算器
 *
 * @author Brozen
 * @since 2021-05-21
 */
@Slf4j
public class FixRateJobScheduleCalculator extends JobScheduleCalculator implements Strategy<Job, Long> {

    /**
     * 作业上下文repository
     */
    private JobContextRepository jobContextRepository;

    protected FixRateJobScheduleCalculator(JobContextRepository jobContextRepository) {
        super(JobScheduleType.FIXED_RATE);
        this.jobContextRepository = jobContextRepository;
    }

    /**
     * 通过此策略计算作业的下一次触发时间戳
     * @param job 作业
     * @return 作业下次触发时间戳
     */
    @Override
    public Long apply(Job job) {

        long now = Instant.now().getEpochSecond();
        long scheduleAt;

        // 未到调度开始时间，不触发下次调度
        JobScheduleOption scheduleOption = job.getScheduleOption();
        long startScheduleAt = calculateStartScheduleTimestamp(scheduleOption);
        if (now < startScheduleAt) {
            return NO_TRIGGER;
        }

        JobContext latestContext = jobContextRepository.getLatestContext(job.getJobId());
        if (latestContext == null) {

            scheduleAt = now;

        } else {

            Duration interval = scheduleOption.getScheduleInterval();
            if (interval == null) {
                log.error("cannot calculate next trigger timestamp of job({}) because interval is not assigned!", job.getJobId());
                return NO_TRIGGER;
            }

            // 已经调度过，则根据调度记录，计算下一次
            long latestContextCreatedAt = latestContext.getCreatedAt().toEpochSecond(ZoneOffset.UTC);
            scheduleAt = latestContextCreatedAt + interval.toMillis();

        }

        return Math.max(scheduleAt, now);
    }


}
