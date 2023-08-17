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

package org.limbo.flowjob.agent.core;

import org.limbo.flowjob.agent.core.worker.Worker;
import org.limbo.flowjob.api.constants.TaskStatus;
import org.limbo.flowjob.api.constants.TaskType;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

/**
 * @author Devil
 * @since 2023/8/8
 */
public class TaskFactory {

    public static Task createTask(String taskId, Job job, Object taskAttributes, TaskType type, Worker worker) {
        Task task = new Task();
        task.setTaskId(taskId);
        task.setJobId(job.getId());
        task.setType(type);
        task.setExecutorName(job.getExecutorName());
        task.setStatus(TaskStatus.SCHEDULING);
        task.setContext(job.getContext());
        task.setJobAttributes(job.getAttributes());
        task.setTaskAttributes(taskAttributes == null ? "" : JacksonUtils.toJSONString(taskAttributes));
        task.setWorker(worker);
        task.setTriggerAt(TimeUtils.currentLocalDateTime());
        return task;
    }

}
