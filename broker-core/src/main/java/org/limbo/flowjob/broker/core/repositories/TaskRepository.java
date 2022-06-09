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

package org.limbo.flowjob.broker.core.repositories;

import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.core.plan.job.context.Task;

/**
 * @author Brozen
 * @since 2021-05-19
 */
public interface TaskRepository {

    /**
     * 持久化作业实例
     * @param task 作业执行实例
     */
    void add(Task task);

    /**
     * CAS 将任务状态从 {@link JobScheduleStatus#SCHEDULING} 更新为 {@link JobScheduleStatus#EXECUTING}
     * @param id 任务ID
     * @return 返回是否更新成功
     */
    boolean execute(Task.ID id);

    /**
     * 更新状态为已反馈
     */
    void executed(Task task);

    /**
     * 更新状态为已完成
     */
    void end(Task task);

    Integer countUnclosed(Task.ID taskId);

    /**
     * 获取作业执行实例
     */
    Task get(String planId, Long planRecordId, Integer planInstanceId, String jobId, Integer jobInstanceId, String taskId);

}
