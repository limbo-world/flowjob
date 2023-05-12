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

package org.limbo.flowjob.common.lb;

import java.util.List;
import java.util.Optional;

/**
 * 负载均衡策略
 *
 * @author Brozen
 * @since 2022-09-02
 */
public interface LBStrategy<S extends LBServer> {


    /**
     * 选择一个服务。
     *
     * @param servers 被负载的服务列表
     * @param invocation 本次调用的上下文信息
     * @return 选择的服务，可能为 null。当无可用服务时，返回 null。
     */
    Optional<S> select(List<S> servers, Invocation invocation);

}
