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

package org.limbo.flowjob.broker.core.domain.task;

import org.limbo.flowjob.api.constants.TaskType;

import java.util.List;

/**
 * @author Devil
 * @since 2023/1/6
 */
public interface TaskManager {

    /**
     * 获取执行结果
     *
     * @param jobInstanceId 节点id
     * @param taskType      任务类型
     * @return 执行结果列表
     */
    List<TaskResult> getTaskResults(String jobInstanceId, TaskType taskType);

}
