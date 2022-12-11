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
 * worker管理器抽象。
 *
 * TIP：和{@link WorkerRepository}的方法定义有些重复，但此接口定义属于tracker domain的方法，应当代理repository；
 * 除此之外，此管理器添加了一些worker domain的监听方法。
 *
 * @author Brozen
 * @since 2021-05-17
 */
public interface WorkerManager {

    /**
     * 注册一个worker，并为worker生成唯一ID
     * @param worker worker节点
     * @return 返回worker
     */
    Worker registerWorker(Worker worker);

    /**
     * 获取所有可用的worker。可用指在调用此方法之时，心跳检测没有失败的worker。
     * @return 可用的worker。
     */
    List<Worker> availableWorkers();

    /**
     * 移除一个worker节点。
     * @param workerId worker id。
     * @return 返回被移除的worker，如果参数id对应的worker不存在，则返回null。
     */
    Worker unregisterWorker(String workerId);

}
