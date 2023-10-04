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

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.common.lb.LBServer;
import org.limbo.flowjob.common.lb.LBServerRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-09-05
 */
@Slf4j
@Accessors(fluent = true)
public class BaseLBServerRepository<S extends LBServer> implements LBServerRepository<S> {

    /**
     * 被负载的服务列表
     */
    private volatile List<S> servers;


    public BaseLBServerRepository(List<S> servers) {
        updateServers(servers);
    }


    /**
     * {@inheritDoc}
     * @param servers 服务列表
     */
    @Override
    public void updateServers(List<S> servers) {
        this.servers = Collections.unmodifiableList(new ArrayList<>(servers));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public List<S> listAliveServers() {
        return this.servers.stream()
                .filter(LBServer::isAlive)
                .collect(Collectors.toList());
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public List<S> listAllServers() {
        return this.servers;
    }

}
