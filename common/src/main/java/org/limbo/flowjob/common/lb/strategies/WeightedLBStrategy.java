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

package org.limbo.flowjob.common.lb.strategies;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.common.lb.LBServer;

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
 * todo 这个感觉应该和其他策略配合使用，如果根据权重找到相同的一批节点，如何对这一批做负载
 */
@Slf4j
public class WeightedLBStrategy<S extends LBServer> extends RoundRobinLBStrategy<S> {

    /**
     * 用于计算权重的函数
     */
    private Function<List<String>, List<Double>> weightSupplier;

    /**
     * 权重选择时的随机数生成器
     */
    private final Random random;


    public WeightedLBStrategy(Function<List<String>, List<Double>> weightSupplier) {
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

    @Override
    public Optional<S> select(List<S> servers) {

        // 有服务存在，但是如果所有服务都挂了的话，也返回空
        if (CollectionUtils.isEmpty(servers)) {
            log.warn("No alive server for load strategy [{}]", getClass().getName());
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
            return super.select(servers);
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
            return Optional.empty();
        }

        return Optional.of(selected);
    }

}
