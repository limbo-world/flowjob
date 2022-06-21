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

import org.limbo.flowjob.broker.api.constants.enums.TaskScheduleStatus;
import org.limbo.flowjob.broker.core.plan.job.context.Task;

import java.util.List;

/**
 * @author Brozen
 * @since 2021-05-19
 */
public interface TaskRepository {

    /**
     * 持久化作业实例
     * @param task 作业执行实例
     */
    String add(Task task);

    /**
     * 将任务状态从 {@link TaskScheduleStatus#SCHEDULING} 更新为 {@link TaskScheduleStatus#EXECUTING}
     * @param taskId 任务ID
     * @return 返回是否更新成功
     */
    boolean execute(String taskId);

    /**
     * 更新状态为已反馈
     */
    void executed(String taskId);

    /**
     * 更新状态为已完成
     */
    void end(Task task);

    /**
     * 根据状态统计数据
     */
    Integer countByStates(String jobInstanceId, List<Byte> states, List<Byte> results);

    /**
     * 获取作业执行实例
     */
    Task get(String planId, Long planRecordId, Integer planInstanceId, String jobId, Integer jobInstanceId, String taskId);

}
