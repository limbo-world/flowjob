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

package org.limbo.flowjob.broker.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * broker节点描述
 *
 * @author Brozen
 * @since 2021-06-16
 */
@Data
@Schema(title = "broker节点描述")
public class BrokerDTO {

    /**
     * tracker节点IP地址
     */
    @Schema(description = "broker节点主机名")
    private String host;

    /**
     * tracker节点服务端口
     */
    @Schema(description = "broker节点服务端口")
    private Integer port;

}
