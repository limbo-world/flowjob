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

package org.limbo.flowjob.tracker.commons.utils.strategies;

/**
 * 策略工厂
 *
 * @author Brozen
 * @since 2021-05-20
 */
public interface StrategyFactory<ST, S extends Strategy<T, R>, T, R> {

    /**
     * 创建一个新的策略.
     * @param strategyType 策略创建的依据
     * @return 新的策略
     */
    S newStrategy(ST strategyType);

}
