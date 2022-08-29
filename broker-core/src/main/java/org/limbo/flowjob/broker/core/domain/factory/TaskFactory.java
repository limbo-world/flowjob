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

package org.limbo.flowjob.broker.core.domain.factory;

import org.limbo.flowjob.broker.api.constants.enums.TaskStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.task.Task;

/**
 * @author Devil
 * @since 2022/8/17
 */
public class TaskFactory {

    public static Task create(JobInstance instance, TaskType type) {
        Task task = new Task();
        task.setJobInstanceId(instance.getJobInstanceId());
        task.setStatus(TaskStatus.DISPATCHING);
        task.setType(type);
        task.setWorkerId("");
        task.setDispatchOption(instance.getDispatchOption());
        task.setExecutorOption(instance.getExecutorOption());
        task.setWorkerManager(null); // todo
        task.setErrorMsg("");
        task.setErrorStackTrace("");
        return task;
    }
}
