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

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Brozen
 * @since 2022-09-02
 */
public abstract class AbstractLBStrategy<S extends LBServer> implements LBStrategy<S> {

    /**
     * 当前负载均衡策略绑定的负载均衡器
     */
    private LoadBalancer<S> loadBalancer;


    /**
     * {@inheritDoc}
     * @param loadBalancer
     */
    @Override
    public void bindWithLoadBalancer(LoadBalancer<S> loadBalancer) {
        this.loadBalancer = Objects.requireNonNull(loadBalancer);
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public LoadBalancer<S> getBoundLoadBalancer() {
        return this.loadBalancer;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Optional<S> choose() {
        return choose(this.loadBalancer);
    }


    /**
     * 结合负载均衡器信息，根据当前负载均衡策略，选择一个服务
     */
    @Nullable
    protected abstract Optional<S> choose(LoadBalancer<S> loadBalancer);
}
