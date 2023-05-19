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

package org.limbo.flowjob.broker.core.worker.rpc;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.broker.core.exceptions.WorkerException;
import org.limbo.flowjob.broker.core.worker.Worker;

import java.util.function.Supplier;

/**
 * @author Devil
 * @since 2022/10/21
 */
@Slf4j
public abstract class AbstractWorkerRpc implements WorkerRpc {

    /**
     * 此 RPC 绑定到的 Worker ID
     */
    private final String workerId;

    /**
     * worker服务使用的通信协议，默认为Http协议。
     * @see Protocol
     */
    private final Protocol protocol;

    /**
     * worker服务的通信host
     */
    private final String host;

    /**
     * worker服务的通信端口
     */
    private final Integer port;

    protected AbstractWorkerRpc(Worker worker) {
        this.workerId = worker.getId();
        this.protocol = null;
        this.host = null;
        this.port = null;
    }

    public <T> T getResponseData(Supplier<ResponseDTO<T>> supplier) {
        ResponseDTO<T> response = supplier.get();
        if (response == null || !response.success()) {
            String msg = response == null ? "response is empty" : String.format("code:%s msg:%s", response.getCode(), response.getMessage());
            throw new WorkerException(workerId, msg);
        }
        return response.getData();
    }

    @Override
    public String workerId() {
        return workerId;
    }

    @Override
    public Protocol protocol() {
        return protocol;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public Integer port() {
        return port;
    }
}
