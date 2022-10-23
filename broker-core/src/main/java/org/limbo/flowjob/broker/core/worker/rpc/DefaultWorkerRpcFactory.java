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

package org.limbo.flowjob.broker.core.worker.rpc;

import org.limbo.flowjob.broker.api.constants.enums.WorkerProtocol;
import org.limbo.flowjob.broker.core.worker.Worker;

import java.net.URL;

/**
 * 默认的 Worker RPC 协议工厂，先在这里写死，后面提取到独立的模块中，通过 SPI 加载。
 *
 * @author Brozen
 * @since 2022-08-12
 */
public class DefaultWorkerRpcFactory implements WorkerRpcFactory {


    /**
     * {@inheritDoc}
     * @param worker
     * @return
     */
    @Override
    public WorkerRpc createRPC(Worker worker) {
        URL rpcBaseUrl = worker.getRpcBaseUrl();
        WorkerProtocol protocol = WorkerProtocol.parse(rpcBaseUrl.getProtocol());
        switch (protocol) {
            case HTTP:
            case HTTPS:
                return new RetrofitHttpWorkerRpc(worker);
            default:
                throw new IllegalArgumentException("不支持的 Worker 通信协议：" + rpcBaseUrl.getProtocol());
        }
    }



}
