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

package org.limbo.flowjob.common.utils.collections;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * 多值 Map，一个 key 可以对应多个 value。根据 value 集合的类型，可以控制同一个 key 的多个 value 是否允许重复。
 *
 * @author Brozen
 * @since 2022-09-20
 */
public interface MultiValueMap<K, V, E extends Collection<V>> extends Map<K, E> {


    /**
     * 添加一个 k-v 映射到此 Map。
     */
    void add(K key, @Nullable V value);


    /**
     * 将 value 集合添加到指定 key 对应的 value 集合中。
     */
    void addAll(K key, Collection<? extends V> values);


    /**
     * 将另一个多值 Map 合并到当前多值 Map 中来。
     */
    void addAll(MultiValueMap<K, V, Collection<V>> values);


    /**
     * {@link #add(Object, Object) 添加} value 到当前 Map 中，仅在 key 不
     * {@link #containsKey(Object) 存在} 于当前 Map 时才会添加。
     */
    default void addIfAbsent(K key, @Nullable V value) {
        if (!containsKey(key)) {
            add(key, value);
        }
    }

}
