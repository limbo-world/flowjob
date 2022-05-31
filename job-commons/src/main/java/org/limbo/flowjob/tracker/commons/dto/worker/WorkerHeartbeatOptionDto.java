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

package org.limbo.flowjob.tracker.commons.dto.worker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * worker 心跳
 *
 * @author Brozen
 * @since 2021-06-10
 */
@Data
@Schema(title = "worker心跳参数")
public class WorkerHeartbeatOptionDto implements Serializable {

    private static final long serialVersionUID = 6512801979734188678L;

    /**
     * worker id
     */
    @Schema(description = "worker id")
    private String workerId;

    /**
     * worker可用的资源
     */
    @Schema(description = "worker可用的资源")
    private WorkerResourceDto availableResource;

}
