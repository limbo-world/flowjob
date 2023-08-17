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

package org.limbo.flowjob.worker.core.rpc;

import org.limbo.flowjob.common.exception.RegisterFailException;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.core.executor.ExecuteContext;

import javax.annotation.Nullable;

/**
 * @author Brozen
 * @since 2022-08-30
 */
public interface WorkerBrokerRpc {

    /**
     * 向 Broker 注册 Worker
     * @param worker 需注册的 Worker
     */
    void register(Worker worker) throws RegisterFailException;


    /**
     * 向 Broker 发送心跳
     * @param worker 发送心跳的 Worker
     */
    void heartbeat(Worker worker);

//    /**
//     * 向 Broker 发送心跳
//     * @param worker 发送心跳的 Worker
//     */
//    void schedulePlan(Worker worker);
//
//    /**
//     * 向 Broker 发送心跳
//     * @param worker 发送心跳的 Worker
//     */
//    void schedulePlanJob(Worker worker);

}
