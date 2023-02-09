/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.core.executor;


import org.limbo.flowjob.common.constants.TaskType;
import org.limbo.flowjob.worker.core.domain.Task;

/**
 * 任务执行器
 *
 * @author Devil
 * @since 2021/7/24
 */
public interface MapReduceTaskExecutor extends MapTaskExecutor {

    @Override
    default void run(Task task) {
        if (TaskType.REDUCE == task.getType()) {
            reduce(task);
        } else {
            MapTaskExecutor.super.run(task);
        }
    }

    /**
     * 处理reduce任务
     * @param task 任务
     */
    void reduce(Task task);

}
