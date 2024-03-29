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

package org.limbo.flowjob.agent.core.rpc;

import org.limbo.flowjob.agent.core.ScheduleAgent;
import org.limbo.flowjob.api.param.broker.AgentHeartbeatParam;
import org.limbo.flowjob.api.param.broker.AgentRegisterParam;
import org.limbo.flowjob.api.param.broker.AgentResourceParam;

/**
 * @author Devil
 * @since 2023/8/4
 */
public class RpcParamFactory {

    /**
     * 封装注册参数
     */
    public static AgentRegisterParam registerParam(ScheduleAgent agent) {
        AgentResourceParam availableResource = new AgentResourceParam();
        availableResource.setAvailableQueueLimit(agent.getResource().availableQueueSize());

        AgentRegisterParam registerParam = new AgentRegisterParam();
        registerParam.setUrl(agent.getURL());
        registerParam.setAvailableResource(availableResource);
        return registerParam;
    }

    /**
     * 封装心跳参数
     */
    public static AgentHeartbeatParam heartbeatParam(ScheduleAgent agent) {
        AgentResourceParam availableResource = new AgentResourceParam();
        availableResource.setAvailableQueueLimit(agent.getResource().availableQueueSize());

        AgentHeartbeatParam registerParam = new AgentHeartbeatParam();
        registerParam.setAvailableResource(availableResource);
        return registerParam;
    }

}
