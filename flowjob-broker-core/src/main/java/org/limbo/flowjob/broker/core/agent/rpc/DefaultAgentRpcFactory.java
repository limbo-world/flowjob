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

import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.broker.core.agent.ScheduleAgent;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.rpc.RetrofitHttpWorkerRpc;
import org.limbo.flowjob.broker.core.worker.rpc.WorkerRpc;
import org.limbo.flowjob.broker.core.worker.rpc.WorkerRpcFactory;

import java.net.URL;

/**
 * 默认的 RPC 协议工厂，先在这里写死，后面提取到独立的模块中，通过 SPI 加载。
 *
 * @author Brozen
 * @since 2022-08-12
 */
public class DefaultAgentRpcFactory implements AgentRpcFactory {


    /**
     * {@inheritDoc}
     * @param agent
     * @return
     */
    @Override
    public AgentRpc createRPC(ScheduleAgent agent) {
        URL rpcBaseUrl = agent.getUrl();
        Protocol protocol = Protocol.parse(rpcBaseUrl.getProtocol());
        switch (protocol) {
            case HTTP:
            case HTTPS:
                return new RetrofitHttpWorkerRpc(worker);
            default:
                throw new IllegalArgumentException("不支持的 Agent 通信协议：" + rpcBaseUrl.getProtocol());
        }
    }



}
