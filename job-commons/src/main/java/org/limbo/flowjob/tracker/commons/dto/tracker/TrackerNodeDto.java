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

package org.limbo.flowjob.tracker.commons.dto.tracker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * tracker节点描述
 *
 * @author Brozen
 * @since 2021-06-16
 */
@Data
@Schema(title = "tracker节点描述")
public class TrackerNodeDto {

    /**
     * tracker节点IP地址
     */
    @Schema(description = "tracker节点主机名")
    private String hostname;

    /**
     * tracker节点服务端口
     */
    @Schema(description = "tracker节点服务端口")
    private Integer port;

}
