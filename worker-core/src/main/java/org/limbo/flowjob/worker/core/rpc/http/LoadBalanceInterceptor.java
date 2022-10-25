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

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.worker.core.rpc.BrokerNode;
import org.limbo.flowjob.worker.core.rpc.LoadBalancer;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Devil
 * @since 2022/10/24
 */
@Slf4j
public class LoadBalanceInterceptor implements Interceptor {

    /**
     * Broker 负载均衡
     */
    private final LoadBalancer<BrokerNode> loadBalancer;

    /**
     * 重试次数
     */
    private final int retryCount;

    public LoadBalanceInterceptor(LoadBalancer<BrokerNode> loadBalancer, int retryCount) {
        this.loadBalancer = loadBalancer;
        this.retryCount = retryCount;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        HttpUrl oldUrl = originalRequest.url();

        for (int i = 1; i <= retryCount; i++) {
            HttpUrl newHttpUrl = getUrl(oldUrl);
            try {
                return chain.proceed(originalRequest.newBuilder().url(newHttpUrl).build());
            } catch (IOException e) {
                log.warn("try {} times... address {} connect fail, try connect new node", i, newHttpUrl, e);
            }
        }
        throw new IOException("try " + retryCount + " times... but also fail, throw to out");
    }

    /**
     * {@inheritDoc}
     * @return
     */
    private BrokerNode choose() {
        while (CollectionUtils.isNotEmpty(this.loadBalancer.listAliveServers())) {
            Optional<BrokerNode> optional = this.loadBalancer.select();
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        throw new IllegalStateException("Can't get alive broker，LB=" + this.loadBalancer.name());
    }

    public HttpUrl getUrl(HttpUrl oldUrl) {
        BrokerNode server = choose();
        HttpUrl baseURL = HttpUrl.get(server.baseUrl);
        return oldUrl.newBuilder()
                .scheme(baseURL.scheme())
                .host(baseURL.host())
                .port(baseURL.port())
                .build();
    }
}
