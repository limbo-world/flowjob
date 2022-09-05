/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.worker.core.rpc.lb;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-09-02
 */
@Slf4j
public class WeightedStrategy<S extends LBServer> extends RoundRobinStrategy<S> {

    /**
     * 用于计算权重的函数
     */
    private Function<List<String>, List<Double>> weightSupplier;

    /**
     * 权重选择时的随机数生成器
     */
    private final Random random;


    public WeightedStrategy(LoadBalancer<S> loadBalancer, Function<List<String>, List<Double>> weightSupplier) {
        super(loadBalancer);
        this.weightSupplier = Objects.requireNonNull(weightSupplier);
        this.random = new Random();
    }


    /**
     * 获取各服务权重
     * @return key：服务ID，value：权重
     */
    protected List<Double> getWeights(List<String> serverIds) {
        List<Double> weights = weightSupplier.apply(serverIds);
        List<Double> addedWeights = new ArrayList<>(weights.size());

        double currWeight = 0d;
        for (Double weight : weights) {
            currWeight += weight;
            addedWeights.add(currWeight);
        }

        return addedWeights;
    }


    /**
     * {@inheritDoc}
     * @param loadBalancer
     * @return
     */
    @Override
    protected Optional<S> choose(LoadBalancer<S> loadBalancer) {


        for (int i = 0; i < 10; i++) {

            List<S> aliveServers = loadBalancer.listAliveServers();
            List<S> servers = loadBalancer.listAllServers();

            // 有服务存在，但是如果所有服务都挂了的话，也返回空
            if (CollectionUtils.isEmpty(aliveServers) || CollectionUtils.isEmpty(servers)) {
                log.warn("No alive server for load balancer [{}] and strategy [{}]",
                        loadBalancer.name(), getClass().getName());
                return Optional.empty();
            }

            // 查询服务权重
            List<String> serverIds = servers.stream()
                    .map(LBServer::getServerId)
                    .collect(Collectors.toList());
            List<Double> weights = getWeights(serverIds);

            // 如权重计算有问题，降级为轮询
            double totalWeights = CollectionUtils.isEmpty(weights) ? 0d : weights.get(weights.size() - 1);
            if (totalWeights < 0.001 || weights.size() != serverIds.size()) {
                return super.choose(loadBalancer);
            }

            // 根据随机权重，选择服务
            int serverIdx = -1, idx = 0;
            double randomWeight = this.random.nextDouble() * totalWeights;
            for (Double weight : weights) {
                if (weight > randomWeight) {
                    serverIdx = idx;
                    break;
                } else {
                    idx++;
                }
            }

            // 服务不存在，重新尝试获取
            S selected = servers.get(serverIdx);
            if (selected == null || !selected.isAlive()) {
                Thread.yield();
                continue;
            }

            return Optional.of(selected);

        }

        // 重试 10 次后也不存在
        log.warn("No available alive servers after 10 tries from load balancer [{}] ", loadBalancer.name());
        return Optional.empty();
    }

}
