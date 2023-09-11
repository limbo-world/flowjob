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

package org.limbo.flowjob.worker.core.rpc;

import org.limbo.flowjob.worker.core.domain.SubTask;
import org.limbo.flowjob.worker.core.domain.Task;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Devil
 * @since 2023/8/7
 */
public interface WorkerAgentRpc {

    Boolean submitSubTasks(Task task, List<SubTask> subTasks);

    /**
     * 反馈任务开始执行
     */
    Boolean reportTaskExecuting(Task task);

    /**
     * 反馈任务执行状态
     */
    Boolean reportTask(Task task);

    /**
     * 反馈任务执行成功
     */
    Boolean feedbackTaskSucceed(Task task);


    /**
     * 反馈任务执行失败
     * @param ex 导致任务失败的异常信息，可以为 null
     */
    Boolean feedbackTaskFailed(Task task, @Nullable Throwable ex);
}
