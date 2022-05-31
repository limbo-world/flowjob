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

package org.limbo.flowjob.broker.core.utils.strategies;

/**
 * 基础策略封装
 *
 * @param <T> 需要应用策略的数据类型
 * @param <R> 对T类型应用策略后，返回的数据类型
 * @author Brozen
 * @since 2021-05-20
 */
public interface Strategy<T, R> {

    /**
     * 此策略是否适用指定数据
     * @param data 数据
     * @return 策略是否适用
     */
    Boolean canApply(T data);

    /**
     * 对数据{@link T}应用策略，并返回{@link R}
     * @param data 数据
     * @return 策略结果
     */
    R apply(T data);

}
