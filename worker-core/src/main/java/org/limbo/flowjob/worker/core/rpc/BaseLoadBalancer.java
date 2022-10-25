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

package org.limbo.flowjob.worker.core.rpc;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.common.lb.LBServer;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-09-05
 */
@Slf4j
@Accessors(fluent = true)
public class BaseLoadBalancer<S extends LBServer> implements LoadBalancer<S> {

    /**
     * 负载均衡器的名称
     */
    @Getter
    private final String name;

    /**
     * 被负载的服务列表
     */
    private volatile List<S> servers;

    /**
     * 负载均衡策略
     */
    private volatile LBStrategy<S> strategy;

    /**
     * 重试次数
     */
    private volatile int retryCount = 10;


    public BaseLoadBalancer(String name, List<S> servers, LBStrategy<S> strategy) {
        this.name = name;
        updateServers(servers);
        updateLBStrategy(strategy);
    }

    public BaseLoadBalancer(String name, int retryCount, List<S> servers, LBStrategy<S> strategy) {
        this(name, servers, strategy);
        this.retryCount = retryCount;
    }

    /**
     * 更新负载均衡策略
     */
    public void updateLBStrategy(LBStrategy<S> strategy) {
        // 默认使用轮询
        if (strategy == null) {
            strategy = new RoundRobinLBStrategy<>();
        }

        this.strategy = strategy;
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
    public Optional<S> select() {
        for (int i = 0; i < retryCount; i++) {
            Optional<S> selected = this.strategy.select(servers);
            if (selected.isPresent()) {
                // todo
            } else {

            }
        }

        log.warn("No available alive servers after 10 tries from load balancer [{}] ", name);
        return Optional.empty();
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
