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

package org.limbo.flowjob.tracker.core;

import org.limbo.flowjob.tracker.core.constants.JobDispatchType;

/**
 * 作业的抽象
 *
 * @author Brozen
 * @since 2021-05-14
 */
public interface Job {

    /**
     * 获取作业ID
     * @return 作业ID
     */
    String id();

    /**
     * 所需的CPU核心数，小于等于0表示此作业未定义CPU需求。在分发作业时，会根据此方法返回的CPU核心需求数量来检测一个worker是否有能力执行此作业。
     * @return 作业所需的CPU核心数
     */
    float cpuRequirement();

    /**
     * 所需的内存GB数，小于等于0表示此作业未定义内存需求。在分发作业时，会根据此方法返回的内存需求数量来检测一个worker是否有能力执行此作业。
     * @return 作业执行所需的内存大小，单位GB。
     */
    float ramRequirement();

    /**
     * 获取作业分发类型。
     * @return 作业分发类型
     */
    JobDispatchType dispatchType();

}
