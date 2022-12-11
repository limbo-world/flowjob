/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.worker.executor;

import org.limbo.flowjob.broker.core.worker.Worker;

import java.util.List;

/**
 * @author Brozen
 * @since 2022-09-21
 */
public interface WorkerExecutorRepository {

    /**
     * 存储 worker 执行器，会删除原有所有执行器，再存储新的执行器
     */
    void save(Worker worker);


    /**
     * 根据 worker 查询执行器
     * @param workerId worker id
     */
    List<WorkerExecutor> list(String workerId);

}
