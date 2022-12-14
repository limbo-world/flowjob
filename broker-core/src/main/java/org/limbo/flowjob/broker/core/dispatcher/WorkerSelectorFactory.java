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

import org.limbo.flowjob.common.constants.LoadBalanceType;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.lb.strategies.AppointLBStrategy;
import org.limbo.flowjob.common.lb.strategies.ConsistentHashLBStrategy;
import org.limbo.flowjob.common.lb.strategies.LFULBStrategy;
import org.limbo.flowjob.common.lb.strategies.LRULBStrategy;
import org.limbo.flowjob.common.lb.strategies.RandomLBStrategy;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;

import java.util.EnumMap;
import java.util.Map;

/**
 * {@link WorkerSelector} 工厂
 *
 * @author Brozen
 * @since 2021-05-18
 */
public class WorkerSelectorFactory {

    private static final Map<LoadBalanceType, WorkerSelector> selectors = new EnumMap<>(LoadBalanceType.class);

    static {
        selectors.put(LoadBalanceType.ROUND_ROBIN, new FilteringWorkerSelector(new RoundRobinLBStrategy<>()));
        selectors.put(LoadBalanceType.RANDOM, new FilteringWorkerSelector(new RandomLBStrategy<>()));
        selectors.put(LoadBalanceType.LEAST_FREQUENTLY_USED, new FilteringWorkerSelector(new LFULBStrategy<>()));
        selectors.put(LoadBalanceType.LEAST_RECENTLY_USED, new FilteringWorkerSelector(new LRULBStrategy<>()));
        selectors.put(LoadBalanceType.APPOINT, new FilteringWorkerSelector(new AppointLBStrategy<>()));
        selectors.put(LoadBalanceType.CONSISTENT_HASH, new FilteringWorkerSelector(new ConsistentHashLBStrategy<>()));

        // TODO 使用 SPI 机制允许第三方扩展 WorkerSelector ?
    }

    /**
     * 根据作业的分发方式，创建一个分发器实例。委托给{@link LoadBalanceType}执行。
     *
     * @param loadBalanceType 分发类型
     * @return 作业分发器
     */
    public static WorkerSelector newSelector(LoadBalanceType loadBalanceType) {
        WorkerSelector workerSelector = selectors.get(loadBalanceType);
        if (workerSelector == null) {
            throw new IllegalArgumentException(MsgConstants.UNKNOWN + " load balance type: " + loadBalanceType);
        }
        return workerSelector;
    }

}
