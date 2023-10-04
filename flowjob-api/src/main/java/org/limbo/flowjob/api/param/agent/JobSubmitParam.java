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

package org.limbo.flowjob.api.param.agent;

import lombok.Data;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.constants.LoadBalanceType;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Devil
 * @since 2023/8/3
 */
@Data
public class JobSubmitParam implements Serializable {
    private static final long serialVersionUID = 3844255455063078620L;

    private String jobInstanceId;

    /**
     * 类型
     *
     * @see JobType
     */
    private Integer type;

    /**
     * 执行器的名称
     */
    private String executorName;

    /**
     * 负载策略
     *
     * @see LoadBalanceType
     */
    private Integer loadBalanceType;

    /**
     * 上下文元数据
     */
    private Map<String, Object> context;

    /**
     * job 属性 plan + job
     */
    private Map<String, Object> attributes;

}
