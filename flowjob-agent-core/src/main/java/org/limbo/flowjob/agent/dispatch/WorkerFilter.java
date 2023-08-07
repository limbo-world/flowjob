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

package org.limbo.flowjob.agent.dispatch;

import org.limbo.flowjob.agent.worker.Worker;

import java.util.List;

/**
 * 基于配置过滤出合适的worker
 */
public interface WorkerFilter {

    /**
     * 选择作业上下文应当下发给的worker。
     *
     * @param args    worker 选择参数
     * @param workers 待下发上下文可用的worker
     */
    List<Worker> filter(WorkerSelectArgument args, List<Worker> workers);

}
