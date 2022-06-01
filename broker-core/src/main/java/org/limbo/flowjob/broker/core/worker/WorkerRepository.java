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

package org.limbo.flowjob.broker.core.worker;

import org.limbo.flowjob.broker.api.constants.enums.WorkerStatus;

import java.util.List;

/**
 * @author Brozen
 * @since 2021-05-19
 */
public interface WorkerRepository {

    /**
     * 新增一个worker
     * @param worker worker节点
     */
    void addWorker(Worker worker);

    /**
     * 更新worker数据
     * @param worker 更新worker
     */
    void updateWorker(Worker worker);

    /**
     * 根据id查询worker
     * @param workerId workerId
     * @return worker节点
     */
    Worker getWorker(String workerId);

    /**
     * 获取所有可用的worker。可用指{@link WorkerStatus#RUNNING}状态的worker。
     * @return 可用的worker。
     */
    List<Worker> availableWorkers();

    /**
     * 移除一个worker
     * @param workerId 需要被移除的workerId
     */
    void removeWorker(String workerId);

}
