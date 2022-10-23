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

package org.limbo.flowjob.broker.api.clent.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author Devil
 * @since 2022/10/20
 */
@Data
@Setter(AccessLevel.NONE)
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class WorkerMetricDTO implements Serializable {

    private static final long serialVersionUID = 1772420318252073416L;

    /**
     * worker节点ID
     */
    private String workerId;

    /**
     * worker节点上正在执行中的作业Id
     */
    private List<String> executingJobs;

    /**
     * worker可用的资源
     */
    private WorkerAvailableResourceDTO availableResource;
}
