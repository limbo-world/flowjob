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

import org.limbo.flowjob.tracker.commons.constants.enums.DispatchType;

/**
 * {@link org.limbo.flowjob.tracker.core.dispatcher.strategies.JobDispatcher} 工厂
 *
 * @author Brozen
 * @since 2021-05-18
 */
public class JobDispatcherFactory {

    /**
     * Double Dispatch (￣▽￣)~* <br/>
     * 根据作业的分发方式，创建一个分发器实例。委托给{@link DispatchType}执行。
     * @param dispatchType 分发类型
     * @return 作业分发器
     */
    public JobDispatcher  newDispatcher(DispatchType dispatchType) {
        switch (dispatchType) {
            case ROUND_ROBIN:
                return new RoundRobinJobDispatcher();

            case RANDOM:
                return new RandomJobDispatcher();

            case LEAST_FREQUENTLY_USED:
                return new LFUJobDispatcher();

            case LEAST_RECENTLY_USED:
                return new LRUJobDispatcher();

            case APPOINT:
                return new AppointJobDispatcher();

            case CONSISTENT_HASH:
                return new ConsistentHashJobDispatcher();

            default:
                return null;
        }
    }

}
