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

import java.io.Serializable;
import java.util.List;

/**
 * @author brozen
 * @since 1.0
 */
public interface Tuple extends Iterable<Object>, Serializable {

    /**
     * 返回元组中元素个数
     */
    int size();

    /**
     * 获取元组中指定下标处的元素
     */
    Object get(int index);

    /**
     * 转数组
     */
    Object[] toArray();

    /**
     * 转列表
     */
    List<Object> toList();

}
