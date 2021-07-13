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

package org.limbo.flowjob.tracker.core.schedule;

import lombok.Data;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.ScheduleOption;
import org.limbo.flowjob.tracker.core.job.context.JobContext;

import java.time.Instant;

/**
 * @author Brozen
 * @since 2021-07-13
 */
@Data
public class SchedulableJob implements Schedulable<JobContext> {

    private Job job;

    private ScheduleOption scheduleOption;

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public JobContext getContext() {
        return job.newContext();
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getId() {
        return job.getJobId();
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public ScheduleOption getScheduleOption() {
        return scheduleOption;
    }


    /**
     * TODO
     * {@inheritDoc}
     * @return
     */
    @Override
    public Instant getLastScheduleAt() {
        return null;
    }


    /**
     * TODO
     * {@inheritDoc}
     * @return
     */
    @Override
    public Instant getLastFeedbackAt() {
        return null;
    }
}
