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

package org.limbo.flowjob.tracker.core.scheduler;

import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleType;

/**
 * 作业调度器，封装了作业的调度流程，根据{@link JobScheduleType}有不同实现。
 *
 * @author Brozen
 * @since 2021-05-18
 */
public interface JobScheduler {

    /**
     * 开始调度一个作业
     * @param job 待调度的作业
     */
    void schedule(Job job);

    /**
     * 停止调度一个作业
     * @param job 停止调度的作业
     */
    void unschedule(Job job);

}
