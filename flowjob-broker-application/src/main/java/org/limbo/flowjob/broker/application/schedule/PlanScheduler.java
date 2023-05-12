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
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.task.Task;

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

    /**
     * 调度 task
     */
    void schedule(Task task);


    /**
     * task 成功处理
     */
    void handleSuccess(Task task, Object result);

    /**
     * task失败处理
     */
    void handleFail(Task task, String errorMsg, String errorStackTrace);

    /**
     * 调度planInstance下对应job
     * @param manualRetry 手工重试
     */
    void scheduleJob(Plan plan, String planInstanceId, String jobId, boolean manualRetry);

    /**
     * plan类型
     * @return 类型
     */
    PlanType getPlanType();

}
