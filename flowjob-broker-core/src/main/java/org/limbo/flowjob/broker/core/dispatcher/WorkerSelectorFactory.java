/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.dispatcher;

import lombok.Setter;
import org.limbo.flowjob.common.constants.LoadBalanceType;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.lb.LBServerStatisticsProvider;
import org.limbo.flowjob.common.lb.strategies.AppointLBStrategy;
import org.limbo.flowjob.common.lb.strategies.ConsistentHashLBStrategy;
import org.limbo.flowjob.common.lb.strategies.LFULBStrategy;
import org.limbo.flowjob.common.lb.strategies.LRULBStrategy;
import org.limbo.flowjob.common.lb.strategies.RandomLBStrategy;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * {@link WorkerSelector} 工厂
 *
 * @author Brozen
 * @since 2021-05-18
 */
public class WorkerSelectorFactory {

    /**
     * 用于获取 LB 服务的统计信息，LRU、LFU 算法会用到。
     * 如果确认不使用 LRU、LFU 算法，可以不设置此属性
     */
    @Setter
    private LBServerStatisticsProvider lbServerStatisticsProvider = LBServerStatisticsProvider.EMPTY_PROVIDER;

    private final Map<LoadBalanceType, Supplier<WorkerSelector>> selectors = new EnumMap<>(LoadBalanceType.class);

    public WorkerSelectorFactory() {
        selectors.put(LoadBalanceType.ROUND_ROBIN, () -> new FilteringWorkerSelector(new RoundRobinLBStrategy<>()));
        selectors.put(LoadBalanceType.RANDOM, () -> new FilteringWorkerSelector(new RandomLBStrategy<>()));
        selectors.put(LoadBalanceType.LEAST_FREQUENTLY_USED, () -> new FilteringWorkerSelector(new LFULBStrategy<>(this.lbServerStatisticsProvider)));
        selectors.put(LoadBalanceType.LEAST_RECENTLY_USED, () -> new FilteringWorkerSelector(new LRULBStrategy<>(this.lbServerStatisticsProvider)));
        selectors.put(LoadBalanceType.APPOINT, () -> new FilteringWorkerSelector(new AppointLBStrategy<>()));
        selectors.put(LoadBalanceType.CONSISTENT_HASH, () -> new FilteringWorkerSelector(new ConsistentHashLBStrategy<>()));
    }

    /**
     * 根据作业的分发方式，创建一个分发器实例。委托给{@link LoadBalanceType}执行。
     *
     * @param loadBalanceType 分发类型
     * @return 作业分发器
     */
    public WorkerSelector newSelector(LoadBalanceType loadBalanceType) {
        return Optional.ofNullable(selectors.get(loadBalanceType))
                .map(Supplier::get)
                .orElseThrow(() -> new IllegalArgumentException(MsgConstants.UNKNOWN + " load balance type: " + loadBalanceType));
    }

}
