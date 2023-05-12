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

package org.limbo.flowjob.broker.core.worker.metric;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.limbo.flowjob.api.remote.param.WorkerResourceParam;

/**
 * worker节点上可用的资源，资源有以下三种定义：内存、CPU。值对象。
 *
 * @author Brozen
 * @since 2021-05-17
 */
@Data
public class WorkerAvailableResource {

    /**
     * 可用的CPU核心数。
     */
    private float availableCpu;

    /**
     * 可用的内存空间，单位GB。
     */
    private float availableRam;

    /**
     * 任务队列剩余可排队数
     */
    private int availableQueueLimit;

    public WorkerAvailableResource(
            @JsonProperty("availableCpu") float availableCpu,
            @JsonProperty("availableRam") float availableRam,
            @JsonProperty("availableQueueLimit") int availableQueueLimit) {
        this.availableCpu = availableCpu;
        this.availableRam = availableRam;
        this.availableQueueLimit = availableQueueLimit;
    }

    /**
     * 将Dto转换为worker资源值对象
     * @param resource 入参worker资源
     * @return 返回worker领域中的资源值对象
     */
    public static WorkerAvailableResource from(WorkerResourceParam resource) {
        return new WorkerAvailableResource(resource.getAvailableCpu(), resource.getAvailableRAM(), resource.getAvailableQueueLimit());
    }

}
