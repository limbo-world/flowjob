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

import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * 包含三个不可变非null元素的元组
 *
 * @author Brozen
 * @since 2019-07-04
 */
@Data
public class Tuple3<A, B, C> implements Tuple {

    private static final long serialVersionUID = -533936270621393166L;

    private A a;

    private B b;

    private C c;

    Tuple3(A a, B b, C c) {
        this.a = requireNonNull(a, "A");
        this.b = requireNonNull(b, "B");
        this.c = requireNonNull(c, "C");
    }

    /**
     * 以指定类型返回元组中第一个元素
     */
    public A getA() {
        return a;
    }

    /**
     * 以指定类型返回元组中第二个元素
     */
    public B getB() {
        return b;
    }

    /**
     * 以指定类型返回元组中第三个元素
     */
    public C getC() {
        return c;
    }

    /**
     * 映射转换元组中第一个元素的值
     */
    public <R> Tuple3<R, B, C> mapA(Function<A, R> fn) {
        return new Tuple3<>(fn.apply(a), b, c);
    }

    /**
     * 映射转换元组中第二个元素的值
     */
    public <R> Tuple3<A, R, C> mapB(Function<B, R> fn) {
        return new Tuple3<>(a, fn.apply(b), c);
    }

    /**
     * 映射转换元组中第三个元素的值
     */
    public <R> Tuple3<A, B, R> mapC(Function<C, R> fn) {
        return new Tuple3<>(a, b, fn.apply(c));
    }

    /**
     * 从三元元组中移除第一个元素，剩下的元素组成二元元组。
     * @return 新的二元元组
     */
    public Tuple2<B, C> removeA() {
        return new Tuple2<>(this.b, this.c);
    }

    /**
     * 从三元元组中移除第二个元素，剩下的元素组成二元元组。
     * @return 新的二元元组
     */
    public Tuple2<A, C> removeB() {
        return new Tuple2<>(this.a, this.c);
    }

    /**
     * 从三元元组中移除第三个元素，剩下的元素组成二元元组。
     * @return 新的二元元组
     */
    public Tuple2<A, B> removeC() {
        return new Tuple2<>(this.a, this.b);
    }

    /**
     * 返回元组中元素个数
     * @return 3
     */
    public int size() {
        return 3;
    }

    /**
     * 根据下标获取元组中的元素
     * @param index 从0开始
     * @return 从0开始的元素，0对应a，依次递推，不在范围内抛出异常
     */
    public Object get(int index) {
        switch (index) {
            case 0: return a;
            case 1: return b;
            case 2: return c;
            default:
                throw new IndexOutOfBoundsException(String.format("size: %s, access: %s", size(), index));
        }
    }

    public List<Object> toList() {
        return Arrays.asList(toArray());
    }

    public Object[] toArray() {
        return new Object[]{ a, b, c, };
    }

    @Override
    public Iterator<Object> iterator() {
        return Collections.unmodifiableList(toList()).iterator();
    }

    @Override
    public int hashCode() {
        int hc = size();
        hc = 31 * hc + a.hashCode();
        hc = 31 * hc + b.hashCode();
        hc = 31 * hc + c.hashCode();
        return hc;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Tuple3)) {
            return false;
        }

        Tuple3<?, ?, ?> other = (Tuple3<?, ?, ?>) obj;
        return a.equals(other.a) && b.equals(other.b) && c.equals(other.c);
    }

    @Override
    public String toString() {
        return Tuples.tupleStringRepresentation(toArray()).insert(0, '[').append(']').toString();
    }
}
