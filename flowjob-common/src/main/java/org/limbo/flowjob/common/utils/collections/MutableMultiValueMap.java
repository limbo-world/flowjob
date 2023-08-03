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

import lombok.experimental.Delegate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Brozen
 * @since 2022-09-20
 */
public class MutableMultiValueMap<K, V, E extends Collection<V>> implements MultiValueMap<K, V, E> {

    /**
     * 内部的 Map 实现
     */
    @Delegate
    private Map<K, E> internalMap;

    /**
     * 用于生成 value 的集合容器
     */
    private Supplier<E> valueFactory;

    /**
     * 最大 value 数量限制
     */
    private final int maxValueSize;


    @SuppressWarnings("unchecked")
    public MutableMultiValueMap() {
        this(() -> (E) new ArrayList<V>());
    }


    public MutableMultiValueMap(Supplier<E> valueFactory) {
        this(new HashMap<>(), valueFactory);
    }


    public MutableMultiValueMap(Map<K, E> map, Supplier<E> valueFactory) {
        this(map, valueFactory, Integer.MAX_VALUE);
    }


    public MutableMultiValueMap(Map<K, E> map, Supplier<E> valueFactory, int maxValueSize) {
        this.internalMap = map;
        this.valueFactory = valueFactory;
        this.maxValueSize = maxValueSize;
    }


    private void ensureValueCollectionSize(E collection, int ensureSize) {
        if (collection.size() + ensureSize > maxValueSize) {
            throw new IllegalStateException("Max values limit to " + maxValueSize);
        }
    }


    /**
     * {@inheritDoc}
     * @param key
     * @param value
     */
    @Override
    public void add(K key, @Nullable V value) {
        E values = internalMap.computeIfAbsent(key, _k -> Objects.requireNonNull(this.valueFactory.get()));
        ensureValueCollectionSize(values, 1);
        values.add(value);
    }


    /**
     * {@inheritDoc}
     * @param key
     * @param values
     */
    @Override
    public void addAll(K key, Collection<? extends V> values) {
        E valueCollection = internalMap.computeIfAbsent(key, _k -> Objects.requireNonNull(this.valueFactory.get()));
        ensureValueCollectionSize(valueCollection, values.size());
    }


    /**
     * {@inheritDoc}
     * @param values
     */
    @Override
    public void addAll(MultiValueMap<K, V, Collection<V>> values) {
        values.forEach(this::addAll);
    }




}
