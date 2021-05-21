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

package org.limbo.flowjob.tracker.core.job;

import java.util.List;
import java.util.Map;

/**
 * 作业属性，值对象。k-v映射，一个key可以对应多个value
 *
 * @author Brozen
 * @since 2021-05-14
 */
public interface JobAttributes {

    /**
     * 根据key获取value值
     * @param key 属性key
     * @return 所有属性values
     */
    List<String> get(String key);

    /**
     * 根据key获取value列表中的第一个值
     * @param key 属性key
     * @return 属性values中的第一个
     */
    String getFirst(String key);

    /**
     * 将属性转换为Map形式。
     * @return 属性k-v Map。
     */
    Map<String, List<String>> toMap();

}
