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

package org.limbo.flowjob.tracker.core.job;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.limbo.flowjob.tracker.commons.constants.enums.JobDispatchType;

/**
 * 作业分发配置，值对象
 *
 * @author Brozen
 * @since 2021-06-01
 */
@Data
@Setter(AccessLevel.NONE)
public class JobDispatchOption {

    /**
     * 作业分发方式
     */
    private JobDispatchType dispatchType;

    /**
     * 所需的CPU核心数，小于等于0表示此作业未定义CPU需求。在分发作业时，会根据此方法返回的CPU核心需求数量来检测一个worker是否有能力执行此作业。
     */
    private float cpuRequirement;

    /**
     * 所需的内存GB数，小于等于0表示此作业未定义内存需求。在分发作业时，会根据此方法返回的内存需求数量来检测一个worker是否有能力执行此作业。
     */
    private float ramRequirement;

    @JsonCreator
    public JobDispatchOption(@JsonProperty("dispatchType") JobDispatchType dispatchType,
                             @JsonProperty("cpuRequirement") float cpuRequirement,
                             @JsonProperty("ramRequirement") float ramRequirement) {
        this.dispatchType = dispatchType;
        this.cpuRequirement = cpuRequirement;
        this.ramRequirement = ramRequirement;
    }
}
