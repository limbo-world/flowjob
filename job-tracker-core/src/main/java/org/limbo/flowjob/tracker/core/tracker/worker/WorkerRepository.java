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

package org.limbo.flowjob.tracker.core.tracker.worker;

import org.limbo.flowjob.tracker.commons.constants.enums.WorkerStatus;

import java.util.List;

/**
 * @author Brozen
 * @since 2021-05-19
 */
public interface WorkerRepository {

    /**
     * 注册一个worker
     * @param worker worker节点
     */
    void addWorker(WorkerDO worker);

    /**
     * 更新worker数据
     * @param worker 更新worker
     */
    void updateWorker(WorkerDO worker);

    /**
     * 获取所有可用的worker。可用指{@link WorkerStatus#RUNNING}状态的worker。
     * @return 可用的worker。
     */
    List<WorkerDO> availableWorkers();

}
