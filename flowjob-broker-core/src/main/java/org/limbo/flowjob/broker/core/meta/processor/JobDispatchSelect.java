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

package org.limbo.flowjob.broker.core.meta.processor;

import org.limbo.flowjob.api.constants.rpc.HttpAgentApi;
import org.limbo.flowjob.broker.core.agent.ScheduleAgent;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;
import org.limbo.flowjob.common.rpc.RPCInvocation;

import java.util.List;

/**
 * @author Devil
 * @since 2024/1/4
 */
public class JobDispatchSelect {

    private static final LBStrategy<ScheduleAgent> lbStrategy = new RoundRobinLBStrategy<>();

    private static final RPCInvocation lbInvocation = RPCInvocation.builder()
            .path(HttpAgentApi.API_JOB_RECEIVE)
            .build();

    public static ScheduleAgent select(List<ScheduleAgent> agents) {
        return lbStrategy.select(agents, lbInvocation).orElse(null);
    }

}
