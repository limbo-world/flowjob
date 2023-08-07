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

package org.limbo.flowjob.broker.application.task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.application.schedule.ScheduleStrategy;
import org.limbo.flowjob.broker.application.support.CommonThreadPool;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;

import java.time.LocalDateTime;

/**
 * 延迟执行的
 *
 * @author pengqi
 * @date 2023/1/9
 */
@Slf4j
public class DelayJobInstanceScheduleTask implements MetaTask {

    @Getter
    private final JobInstance jobInstance;

    /**
     * 期望的触发时间
     */
    @Getter
    private final LocalDateTime triggerAt;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private final ScheduleStrategy scheduleStrategy;

    public DelayJobInstanceScheduleTask(JobInstance jobInstance, ScheduleStrategy scheduleStrategy) {
        this.jobInstance = jobInstance;
        this.triggerAt = jobInstance.getTriggerAt();
        this.scheduleStrategy = scheduleStrategy;
    }

    @Override
    public void execute() {
        CommonThreadPool.IO.submit(() -> {
            try {
                scheduleStrategy.schedule(jobInstance);
            } catch (Exception e) {
                log.error("jobInstance {} schedule fail", jobInstance.getJobInstanceId(), e);
            }
        });
    }

    @Override
    public MetaTaskType getType() {
        return MetaTaskType.DELAY_JOB;
    }

    @Override
    public String getMetaId() {
        return jobInstance.getJobInstanceId();
    }

    @Override
    public LocalDateTime scheduleAt() {
        return triggerAt;
    }
}
