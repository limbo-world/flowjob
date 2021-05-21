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

package org.limbo.flowjob.tracker.core.job;

import lombok.Setter;
import org.limbo.flowjob.tracker.core.executor.dispatcher.JobDispatchType;
import org.limbo.flowjob.tracker.core.job.schedule.JobTriggerCalculator;

/**
 * @author Brozen
 * @since 2021-05-20
 */
public class SimpleJob implements Job {

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * CPU内核需求数量
     */
    private float cpuRequirement;

    /**
     * 内存需求数量
     */
    private float ramRequirement;

    /**
     * 作业调度方式
     */
    private JobScheduleType scheduleType;

    /**
     * 作业分发方式
     */
    private JobDispatchType dispatchType;

    /**
     * 作业触发计算器
     */
    @Setter
    private JobTriggerCalculator triggerCalculator;

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String id() {
        return jobId;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public float cpuRequirement() {
        return cpuRequirement;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public float ramRequirement() {
        return ramRequirement;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public JobScheduleType scheduleType() {
        return scheduleType;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public JobDispatchType dispatchType() {
        return dispatchType;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public long nextTriggerAt() {
        return triggerCalculator.apply(this);
    }

    @Override
    public JobContext newContext() {
        // TODO
        return null;
    }

    @Override
    public JobContext getContext(String contextId) {
        // TODO
        return null;
    }
}
