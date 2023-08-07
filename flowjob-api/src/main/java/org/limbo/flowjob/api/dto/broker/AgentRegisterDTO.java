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

package org.limbo.flowjob.api.dto.broker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * agent注册结果
 *
 * @author Brozen
 * @since 2021-06-16
 */
@Data
@Schema(title = "agent注册结果")
public class AgentRegisterDTO {

    /**
     * 节点 ID
     */
    @Schema(description = "ID")
    private String agentId;

    /**
     * Broker 的拓扑结构
     */
    @Schema(description = "broker 的拓扑结构")
    private BrokerTopologyDTO brokerTopology;

}
