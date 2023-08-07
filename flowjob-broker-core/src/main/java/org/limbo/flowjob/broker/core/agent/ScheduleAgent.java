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

package org.limbo.flowjob.broker.core.agent;

import lombok.experimental.Delegate;
import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.broker.core.agent.rpc.AgentRpc;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.worker.rpc.WorkerRpc;
import org.limbo.flowjob.broker.core.worker.rpc.WorkerRpcFactory;
import org.limbo.flowjob.common.lb.LBServer;

import java.net.URL;

/**
 * @author Devil
 * @since 2023/8/7
 */
public class ScheduleAgent implements AgentRpc, LBServer {

    /**
     * RPC 通信协议
     */
    private volatile AgentRpc rpc;

    /**
     * 懒加载 Worker RPC 模块
     */
    @Delegate(types = WorkerRpc.class)
    private synchronized WorkerRpc getRPC() {
        if (this.rpc == null) {
            WorkerRpcFactory factory = WorkerRpcFactory.getInstance();
            this.rpc = factory.createRPC(this);
        }

        return this.rpc;
    }

    @Override
    public String getServerId() {
        return null;
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public boolean dispatch(JobInstance instance) {
        return false;
    }

    @Override
    public String agentId() {
        return null;
    }

    @Override
    public Protocol protocol() {
        return null;
    }

    @Override
    public String host() {
        return null;
    }

    @Override
    public Integer port() {
        return null;
    }
}
