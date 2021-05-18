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

package org.limbo.flowjob.tracker.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * worker管理器
 *
 * @author Brozen
 * @since 2021-05-17
 */
public interface WorkerManager {

    /**
     * 注册一个worker，并为worker生成唯一ID
     * @param worker worker节点
     * @return 返回workerID
     */
    Mono<String> registerWorker(Worker worker);

    /**
     * 获取所有可用的worker。可用指在调用此方法之时，心跳检测没有失败的worker。
     * @return 可用的worker。
     */
    List<Worker> availableWorkers();

    /**
     * 移除一个worker节点。
     * @param id worker id。
     * @return 返回被移除的worker，如果参数id对应的worker不存在，则返回null。
     */
    Mono<Worker> removeWorker(String id);

    /**
     * 新的worker被注册时。
     * @return 新worker注册时触发时的Mono
     */
    Flux<Worker> onNewWorkerRegistered();

    /**
     * 当worker被移除时。
     * @return worker被移除时触发的Mono
     */
    Flux<Worker> onWorkerRemoved();

}
