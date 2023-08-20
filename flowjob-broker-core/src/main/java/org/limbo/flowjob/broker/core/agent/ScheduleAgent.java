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

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.limbo.flowjob.api.constants.AgentStatus;
import org.limbo.flowjob.broker.core.agent.rpc.AgentRpc;
import org.limbo.flowjob.broker.core.agent.rpc.AgentRpcFactory;
import org.limbo.flowjob.common.lb.LBServer;

import java.net.URL;

/**
 * @author Devil
 * @since 2023/8/7
 */
@Getter
@Setter(AccessLevel.NONE)
@ToString
@Builder(builderClassName = "Builder")
public class ScheduleAgent implements AgentRpc, LBServer {

    /**
     * ID
     */
    private String id;

    /**
     * 通信的基础 URL
     */
    private URL rpcBaseUrl;

    /**
     * 节点状态
     */
    private AgentStatus status;

    /**
     * RPC 通信协议
     */
    private volatile AgentRpc rpc;

    /**
     * 懒加载 Worker RPC 模块
     */
    @Delegate(types = AgentRpc.class)
    private synchronized AgentRpc getRPC() {
        if (this.rpc == null) {
            AgentRpcFactory factory = AgentRpcFactory.getInstance();
            this.rpc = factory.createRPC(this);
        }

        return this.rpc;
    }

    @Override
    public String getServerId() {
        return id;
    }

    @Override
    public boolean isAlive() {
        return AgentStatus.RUNNING == status;
    }

    @Override
    public URL getUrl() {
        return rpcBaseUrl;
    }

}
