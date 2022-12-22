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
import org.limbo.flowjob.common.lb.AbstractLBStrategy;
import org.limbo.flowjob.common.lb.Invocation;
import org.limbo.flowjob.common.lb.LBServer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-09-02
 */
// todo 这个计算方式有问题
@Slf4j
public class RoundRobinLBStrategy<S extends LBServer> extends AbstractLBStrategy<S> {

    /**
     * 用于计算权重的函数，如果不考虑权重，仅轮训，则为所有服务返回权重 1 即可。
     */
    private final Function<List<S>, Map<String, Integer>> weightSupplier;

    /**
     * 记录轮询索引
     */
    private final ConcurrentHashMap<String, Map<String, RoundRobinIndexer>> indexers;


    public RoundRobinLBStrategy() {
        this(s -> s.stream().collect(Collectors.toMap(LBServer::getServerId, a -> 1)));
    }


    public RoundRobinLBStrategy(Function<List<S>, Map<String, Integer>> weightSupplier) {
        this.weightSupplier = weightSupplier;
        this.indexers = new ConcurrentHashMap<>();
    }


    /**
     * {@inheritDoc}
     * @param servers 被负载的服务列表，可以保证非空。
     * @param invocation 本次调用的上下文信息
     * @return
     */
    @Override
    protected Optional<S> doSelect(List<S> servers, Invocation invocation) {

        String targetId = invocation.getInvokeTargetId();
        Map<String, RoundRobinIndexer> indexerMap =
                this.indexers.computeIfAbsent(targetId, _k -> new ConcurrentHashMap<>());

        long now = System.currentTimeMillis();
        Map<String, Integer> weights = weightSupplier.apply(servers);

        long maxWeight = Long.MIN_VALUE;
        long allWeight = 0;
        S selected = null;
        RoundRobinIndexer selectedIndexer = null;
        for (S server : servers) {
            int weight = Math.max(weights.getOrDefault(server.getServerId(), 0), 0);
            RoundRobinIndexer indexer = indexerMap
                    .computeIfAbsent(server.getServerId(), _sid -> new RoundRobinIndexer(weight));

            if (indexer.weight != weight) {
                indexer.weight = weight;
            }

            long curr = indexer.stepForward();
            if (curr > maxWeight) {
                maxWeight = curr;
                selected = server;
                selectedIndexer = indexer;
            }

            indexer.updatedAt = now;
            allWeight += weight;
        }

        if (selected != null) {
            selectedIndexer.reset(allWeight);
            return Optional.of(selected);
        }

        return Optional.of(servers.get(0));
    }



    static class RoundRobinIndexer {

        int weight;

        AtomicLong current = new AtomicLong(0);

        long updatedAt = 0;

        public RoundRobinIndexer(int weight) {
            this.weight = weight;
        }

        long stepForward() {
            return current.addAndGet(weight);
        }

        void reset(long step) {
            current.addAndGet(-step);
        }

    }

}
