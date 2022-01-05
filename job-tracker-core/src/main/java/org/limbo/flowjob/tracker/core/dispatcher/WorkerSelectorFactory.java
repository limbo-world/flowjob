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

package org.limbo.flowjob.tracker.core.dispatcher;

import org.limbo.flowjob.tracker.commons.constants.enums.LoadBalanceType;
import org.limbo.flowjob.tracker.core.dispatcher.strategies.*;

import javax.inject.Inject;

/**
 * {@link WorkerSelector} 工厂
 *
 * @author Brozen
 * @since 2021-05-18
 */
public class WorkerSelectorFactory {

    @Inject
    private RoundRobinWorkerSelector roundRobinWorkerSelector;

    /**
     * Double Selector (￣▽￣)~* <br/>
     * 根据作业的分发方式，创建一个分发器实例。委托给{@link LoadBalanceType}执行。
     *
     * @param loadBalanceType 分发类型
     * @return 作业分发器
     */
    public WorkerSelector newSelector(LoadBalanceType loadBalanceType) {
        switch (loadBalanceType) {
            case ROUND_ROBIN:
                return roundRobinWorkerSelector;

            case RANDOM:
                return new RandomWorkerSelector();

            case LEAST_FREQUENTLY_USED:
                return new LFUWorkerSelector();

            case LEAST_RECENTLY_USED:
                return new LRUWorkerSelector();

            case APPOINT:
                return new AppointWorkerSelector();

            case CONSISTENT_HASH:
                return new ConsistentHashWorkerSelector();

            default:
                throw new IllegalArgumentException("Unknown load balance type: " + loadBalanceType);
        }
    }

}
