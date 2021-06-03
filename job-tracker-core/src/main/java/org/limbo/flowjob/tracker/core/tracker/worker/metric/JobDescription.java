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

package org.limbo.flowjob.tracker.core.tracker.worker.metric;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 作业的描述信息，用于tracker和worker之间的通信。
 *
 * @author Brozen
 * @since 2021-05-17
 */
@Data
@Setter(AccessLevel.NONE)
public class JobDescription {

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * 作业所需的CPU核心数
     */
    private float cpuRequirement;

    /**
     * 作业所需的内存GB数
     */
    private float ramRequirement;

    /**
     * 作业属性，可用于分片作业、MapReduce作业、DAG工作流进行传参。
     */
    private Map<String, List<String>> attributes;

    @JsonCreator
    public JobDescription(@JsonProperty("jobId") String jobId,
                          @JsonProperty("cpuRequirement") float cpuRequirement,
                          @JsonProperty("ramRequirement") float ramRequirement,
                          @JsonProperty("attributes") Map<String, List<String>> attributes) {
        this.jobId = jobId;
        this.cpuRequirement = cpuRequirement;
        this.ramRequirement = ramRequirement;
        this.attributes = attributes;
    }

}
