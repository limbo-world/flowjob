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

package org.limbo.flowjob.worker.core.resource;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.limbo.flowjob.worker.core.domain.WorkerResources;
import org.limbo.flowjob.worker.core.executor.TaskRepository;

/**
 * @author Brozen
 * @since 2022-09-05
 */
@Getter
@Accessors(fluent = true)
public abstract class AbstractWorkerResources implements WorkerResources {

    /**
     * 并发执行任务数量
     */
    private int concurrency;

    /**
     * 可分配任务总数
     */
    private int queueSize;

    /**
     * 任务仓库
     */
    private TaskRepository taskRepository;

    public AbstractWorkerResources(int concurrency, int queueSize) {
        this.concurrency = concurrency;
        this.queueSize = queueSize;
        this.taskRepository = new TaskRepository();
    }


    /**
     * {@inheritDoc}
     * @return
     */
    public int availableQueueSize() {
        return queueSize - taskRepository.count();
    }


}
