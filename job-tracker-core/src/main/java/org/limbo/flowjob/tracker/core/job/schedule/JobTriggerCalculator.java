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

import org.limbo.flowjob.tracker.core.commons.Strategy;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.JobScheduleType;

/**
 * 任务调度计算策略，用于计算下次触发时间
 *
 * @author Brozen
 * @since 2021-05-20
 */
public abstract class JobTriggerCalculator implements Strategy<Job, Long> {

    /**
     * 此策略适用的作业调度类型
     */
    private final JobScheduleType scheduleType;

    protected JobTriggerCalculator(JobScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    /**
     * 此策略是否适用作业
     * @param job 作业
     * @return 是否可用此策略计算作业的触发时间
     */
    @Override
    public Boolean canApply(Job job) {
        return job.scheduleType() == this.scheduleType;
    }

    /**
     * 通过此策略计算作业的下一次触发时间戳
     * @param job 作业
     * @return 作业下次触发时间戳
     */
    @Override
    public abstract Long apply(Job job);

}
