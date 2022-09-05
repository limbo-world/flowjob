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

package org.limbo.flowjob.worker.core.domain;

/**
 * @author Brozen
 * @since 2022-09-05
 */
public interface WorkerResources {

    /**
     * 可分配任务总数
     */
    int queueSize();

    /**
     * 剩余可分配任务数
     */
    int availableQueueSize();

    /**
     * 可用 CPU 核数
     */
    float availableCpu();

    /**
     * 可用的 RAM 内存数量
     */
    long availableRam();

}
