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

package org.limbo.flowjob.common.utils.tuple;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 键值对
 *
 * @author Brozen
 * @since 1.0.4 继承{@link Map.Entry}
 */
public class Entry<K, V> implements Map.Entry<K, V> {

    /**
     * key
     */
    private final K key;

    /**
     * value
     */
    private V value;


    public Entry(K key, V value) {
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }


    /**
     * 获取key
     */
    public K getKey() {
        return key;
    }


    /**
     * 获取value
     */
    public V getValue() {
        return value;
    }


    /**
     * {@inheritDoc}
     * @param value
     * @return
     */
    @Override
    public V setValue(V value) {
        V oldValue = this.value;
        this.value = Objects.requireNonNull(value);
        return oldValue;
    }


    /**
     * 将key转换为另一个值
     * @param mapper 转换函数
     * @param <NK> 转换后的新key类型
     * @return 转换后的新键值对
     */
    public <NK> Entry<NK, V> mapKey(Function<K, NK> mapper) {
        return new Entry<>(Objects.requireNonNull(mapper).apply(key), value);
    }


    /**
     * 将value转换为另一个值
     * @param mapper 转换函数
     * @param <NV> 转换后的新value类型
     * @return 转换后的新键值对
     */
    public <NV> Entry<K, NV> mapValue(Function<V, NV> mapper) {
        return new Entry<>(key, Objects.requireNonNull(mapper).apply(value));
    }

}
