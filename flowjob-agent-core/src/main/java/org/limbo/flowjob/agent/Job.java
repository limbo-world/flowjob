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

package org.limbo.flowjob.agent;

import lombok.Data;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.constants.LoadBalanceType;
import org.limbo.flowjob.common.utils.attribute.Attributes;

/**
 * @author Devil
 * @since 2023/8/4
 */
@Data
public class Job {

    /**
     * 实例id
     */
    private String id;

    /**
     * 类型
     *
     * @see JobType
     */
    private JobType type;

    /**
     * 执行器的名称
     */
    private String executorName;

    /**
     * 负载策略
     */
    private LoadBalanceType loadBalanceType;

    /**
     * 上下文元数据
     */
    private Attributes context;

    /**
     * job配置的属性
     */
    private Attributes attributes;

}
