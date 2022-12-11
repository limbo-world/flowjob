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

package org.limbo.flowjob.broker.core.cluster;

import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;

import java.util.List;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class WorkerManagerImpl implements WorkerManager {
    /**
     * 用户管理worker，实现WorkerManager的相关功能
     */
    private final WorkerRepository workerRepository;

    public WorkerManagerImpl(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    /**
     * {@inheritDoc}
     *
     * @param worker worker节点
     * @return
     */
    @Override
    public Worker registerWorker(Worker worker) {
//        workerRepository.save(worker);
        return worker;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<Worker> availableWorkers() {
        return workerRepository.listAvailableWorkers();
    }

    /**
     * {@inheritDoc}
     *
     * @param workerId worker id。
     * @return
     */
    @Override
    public Worker unregisterWorker(String workerId) {
        Worker worker = workerRepository.get(workerId);
        workerRepository.delete(workerId);
        return worker;
    }
}
