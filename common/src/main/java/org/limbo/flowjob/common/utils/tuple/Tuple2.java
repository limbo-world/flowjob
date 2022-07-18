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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 包含两个不可变非null元素的元组
 *
 * @author Brozen
 * @since 2019-07-04
 */
public class Tuple2<A, B> implements Tuple {

    private static final long serialVersionUID = 6200855464344145828L;

    private final A a;

    private final B b;

    Tuple2(A a, B b) {
        this.a = Objects.requireNonNull(a, "A");
        this.b = Objects.requireNonNull(b, "B");
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
     * 映射转换元组中第一个元素的值
     */
    public <R> Tuple2<R, B> mapA(Function<A, R> fn) {
        return new Tuple2<>(fn.apply(a), b);
    }

    /**
     * 映射转换元组中第二个元素的值
     */
    public <R> Tuple2<A, R> mapB(Function<B, R> fn) {
        return new Tuple2<>(a, fn.apply(b));
    }

    /**
     * 交换元组中元素的位置，返回新的二元元组。
     * @return 新的二元元组
     */
    public Tuple2<B, A> reverse() {
        return new Tuple2<>(this.b, this.a);
    }

    /**
     * 返回元组中元素个数
     * @return 2
     */
    public int size() {
        return 2;
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
            default:
                throw new IndexOutOfBoundsException(String.format("size: %s, access: %s", size(), index));
        }
    }

    /**
     * 添加一个元素到元组，生成新的三元元组
     * @param c 三元元组的第三个元素
     * @param <C> 第三个元素类型
     * @return 新的三元元组
     */
    public <C> Tuple3<A, B, C> add(C c) {
        return new Tuple3<>(this.a, this.b, c);
    }

    public List<Object> toList() {
        return Arrays.asList(toArray());
    }

    public Object[] toArray() {
        return new Object[]{ a, b, };
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
        return hc;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Tuple2)) {
            return false;
        }

        Tuple2<?, ?> other = (Tuple2<?, ?>) obj;
        return a.equals(other.a) && b.equals(other.b);
    }

    @Override
    public String toString() {
        return Tuples.tupleStringRepresentation(toArray()).insert(0, '[').append(']').toString();
    }
}
