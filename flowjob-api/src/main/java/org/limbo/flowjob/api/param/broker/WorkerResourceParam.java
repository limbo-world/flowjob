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

package org.limbo.flowjob.api.param.broker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * worker节点上可用的资源
 *
 * @author Brozen
 * @since 2021-05-17
 */
@Data
@Schema(title = "worker节点的资源描述")
public class WorkerResourceParam {

    /**
     * 可用的CPU核心数。
     */
    @Schema(description = "可用的CPU核心数。")
    private float availableCpu;

    /**
     * 可用的内存空间，单位MB。
     */
    @Schema(description = "可用的内存空间，单位MB。")
    private float availableRAM;

    /**
     * 任务队列剩余可排队数
     */
    @Schema(description = "任务队列可排队数")
    private int availableQueueLimit;

}
