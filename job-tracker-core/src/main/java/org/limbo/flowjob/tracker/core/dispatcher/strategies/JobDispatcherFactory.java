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

package org.limbo.flowjob.tracker.core.dispatcher.strategies;

import org.limbo.flowjob.tracker.commons.constants.enums.LoadBalanceType;

import javax.inject.Inject;

/**
 * {@link Dispatcher} 工厂
 *
 * @author Brozen
 * @since 2021-05-18
 */
public class JobDispatcherFactory {

    @Inject
    private RoundRobinDispatcher roundRobinDispatcher;

    /**
     * 根据作业的分发方式，创建一个分发器实例。委托给{@link LoadBalanceType}执行。
     *
     * @param loadBalanceType 分发类型
     * @return 作业分发器
     */
    public Dispatcher newDispatcher(LoadBalanceType loadBalanceType) {
        switch (loadBalanceType) {
            case ROUND_ROBIN:
                return roundRobinDispatcher;

            case RANDOM:
                return new RandomDispatcher();

            case LEAST_FREQUENTLY_USED:
                return new LFUDispatcher();

            case LEAST_RECENTLY_USED:
                return new LRUDispatcher();

            case APPOINT:
                return new AppointDispatcher();

            case CONSISTENT_HASH:
                return new ConsistentHashDispatcher();

            default:
                throw new IllegalArgumentException("Unknown load balance type: " + loadBalanceType);
        }
    }

}
