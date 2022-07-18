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

/**
 * @author brozen
 * @since 2020-12-07
 */
public class Tuples {

    /**
     * 创建一个二元元组
     */
    public static <A, B> Tuple2<A, B> of(A a, B b) {
        return new Tuple2<>(a, b);
    }

    /**
     * 创建一个三元元组
     */
    public static <A, B, C> Tuple3<A, B, C> of(A a, B b, C c) {
        return new Tuple3<>(a, b, c);
    }

    /**
     * 将数组拼接为字符串的处理，用于元组的toString
     */
    static StringBuilder tupleStringRepresentation(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (Object t : values) {
            sb.append(',');
            if (t != null) {
                sb.append(t);
            }
        }
        // 移除最后一个逗号
        sb.setLength(sb.length() - 1);
        return sb;
    }

}
