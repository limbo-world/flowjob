/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.api.dto.worker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.limbo.flowjob.broker.api.dto.broker.BrokerDTO;

import java.util.List;

/**
 * worker注册结果
 *
 * @author Brozen
 * @since 2021-06-16
 */
@Data
@Schema(title = "worker注册结果")
public class WorkerRegisterDTO {

    @Schema(description = "workerId的字符串形式，由protocol、ip、port决定")
    private String workerId;

    @Schema(description = "broker节点列表，主从模式下，列表中仅包括一个主节点")
    private List<BrokerDTO> brokers;

}
