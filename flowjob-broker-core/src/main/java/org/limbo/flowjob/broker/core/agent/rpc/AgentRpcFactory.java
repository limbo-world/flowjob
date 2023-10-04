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

package org.limbo.flowjob.broker.core.agent.rpc;

import org.limbo.flowjob.broker.core.agent.ScheduleAgent;

/**
 * @author Brozen
 * @since 2022-08-12
 */
public interface AgentRpcFactory {


    /**
     * 获取 RPC 协议工厂，使用 SPI 加载。
     */
    static AgentRpcFactory getInstance() {
        return AgentRpcFactoryHolder.INSTANCE;
    }



    /**
     * 生成 Worker 的 RPC 通信协议。
     */
    AgentRpc createRPC(ScheduleAgent agent);



}
