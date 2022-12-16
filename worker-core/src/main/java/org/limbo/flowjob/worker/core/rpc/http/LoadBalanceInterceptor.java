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

package org.limbo.flowjob.worker.core.rpc.http;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.limbo.flowjob.common.lb.LBServer;
import org.limbo.flowjob.common.lb.LBServerRepository;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.lb.RPCInvocation;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2022/10/24
 */
@Slf4j
@Accessors(fluent = true)
public class LoadBalanceInterceptor<S extends LBServer> implements Interceptor {

    /**
     * 被负载的服务列表
     */
    private LBServerRepository<S> repository;

    /**
     * 负载均衡策略
     */
    private LBStrategy<S> strategy;

    /**
     * 重试次数
     */
    private volatile int retryCount = 10;

    public LoadBalanceInterceptor(LBServerRepository<S> repository, LBStrategy<S> strategy) {
        this.repository = repository;
        updateLBStrategy(strategy);
    }

    public LoadBalanceInterceptor(int retryCount, LBServerRepository<S> repository, LBStrategy<S> strategy) {
        this(repository, strategy);
        this.retryCount = retryCount;
    }

    @Override
    public Response intercept(Chain chain) {
        Request originalRequest = chain.request();
        HttpUrl oldUrl = originalRequest.url();

        List<S> servers = repository.listAliveServers();
        for (int i = 1; i <= retryCount; i++) {
            Optional<S> optional = strategy.select(servers, new RPCInvocation(oldUrl.url().getPath()));
            if (!optional.isPresent()) {
                log.warn("No available alive servers after 10 tries from load balancer");
                throw new IllegalStateException("Can't get alive broker");
            }
            S select = optional.get();
            HttpUrl baseURL = HttpUrl.get(select.getUrl());
            HttpUrl newHttpUrl = oldUrl.newBuilder()
                    .scheme(baseURL.scheme())
                    .host(baseURL.host())
                    .port(baseURL.port())
                    .build();
            try {
                return chain.proceed(originalRequest.newBuilder().url(newHttpUrl).build());
            } catch (IOException e) {
                log.warn("try {} times... address {} connect fail, try connect new node", i, newHttpUrl, e);
                servers = servers.stream().filter(s -> !s.getServerId().equals(select.getServerId())).collect(Collectors.toList());
            }

        }
        throw new IllegalStateException("try " + retryCount + " times... but also fail, throw to out");
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


}
