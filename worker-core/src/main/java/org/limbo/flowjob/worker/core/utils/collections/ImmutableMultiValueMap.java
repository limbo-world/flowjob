/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.worker.core.utils.collections;

import lombok.experimental.Delegate;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 装饰器模式实现的不可变多值 Map。
 * 会先将被代理的 Map 通过 {@link Collections#unmodifiableMap(Map)} 方法封装，
 * 然后此 Map 拦截了所有的 {@link MultiValueMap} 的写入方法调用。
 *
 * @author Brozen
 * @since 2022-09-20
 */
public class ImmutableMultiValueMap<K, V, E extends Collection<V>> implements MultiValueMap<K, V, E> {

    /**
     * 被装饰的对象。
     */
    @Delegate
    private final Map<K, E> internal;


    /**
     * 根据入参多值 Map 封装生成一个不可变多值 Map。
     */
    public ImmutableMultiValueMap(MultiValueMap<K, V, E> internal) {
        this.internal = Collections.unmodifiableMap(internal);
    }


    /**
     * 不可调用此方法，将抛出 {@link UnsupportedOperationException}
     */
    @Override
    public void add(K key, @Nullable V value) {
        throw new UnsupportedOperationException();
    }


    /**
     * 不可调用此方法，将抛出 {@link UnsupportedOperationException}
     */
    @Override
    public void addAll(K key, Collection<? extends V> values) {
        throw new UnsupportedOperationException();
    }


    /**
     * 不可调用此方法，将抛出 {@link UnsupportedOperationException}
     */
    @Override
    public void addAll(MultiValueMap<K, V, Collection<V>> values) {
        throw new UnsupportedOperationException();
    }

}
