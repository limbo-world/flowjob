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
 * 负载均衡器
 *
 * @author Brozen
 * @since 2022-09-02
 */
public interface LoadBalancer<S extends LBServer> {

    /**
     * 负载均衡器的名称
     */
    String name();


    /**
     * 更新被负载的服务列表
     * @param servers 服务列表
     */
    void updateServers(List<S> servers);


    /**
     * 从当前负载均衡器中，选择服务
     */
    Optional<S> choose();


    /**
     * 列出负载均衡器中所有存活的服务
     */
    List<S> listAliveServers();


    /**
     * 列出负载均衡器中所有服务
     */
    List<S> listAllServers();

}
