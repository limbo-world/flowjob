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

package org.limbo.flowjob.broker.application.schedule;

import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.common.meta.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.Plan;

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2023/5/8
 */
public interface PlanScheduler {

    /**
     * 调度plan实例
     */
    void schedule(Plan plan, String planInstanceId, LocalDateTime triggerAt);

    /**
     * 调度job实例
     */
    void schedule(JobInstance jobInstance);

    void handleJobSuccess(JobInstance jobInstance);

    void handleJobFail(JobInstance jobInstance, String errorMsg);

    /**
     * 调度planInstance下对应job
     */
    void scheduleJob(Plan plan, String planInstanceId, String jobId);


    /**
     * 手工调度planInstance下对应job
     */
    void manualScheduleJob(Plan plan, String planInstanceId, String jobId);

    /**
     * plan类型
     * @return 类型
     */
    PlanType getPlanType();

}
