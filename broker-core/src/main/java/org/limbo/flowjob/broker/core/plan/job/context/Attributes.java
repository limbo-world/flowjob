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

package org.limbo.flowjob.broker.core.plan.job.context;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作业属性，值对象。k-v映射，一个key可以对应多个value
 *
 * @author Brozen
 * @since 2021-05-21
 */
public class Attributes implements Serializable {

    private static final long serialVersionUID = -8346798332193684079L;

    /**
     * 内部数据结构
     */
    private final Map<String, List<String>> attributes;

    public Attributes() {
        this.attributes = new HashMap<>();
    }

    public Attributes(String attr) {
        this.attributes = JacksonUtils.parseObject(attr, new TypeReference<Map<String, List<String>>>() {
        });
    }

    public Attributes(Map<String, List<String>> attributes) {
        this.attributes = attributes == null ? new HashMap<>() : new HashMap<>(attributes);
    }

    /**
     * 根据key获取value值
     * @param key 属性key
     * @return 所有属性values
     */
    public List<String> get(String key) {
        return attributes.get(key);
    }

    /**
     * 根据key获取value列表中的第一个值
     * @param key 属性key
     * @return 属性values中的第一个
     */
    public String getFirst(String key) {
        List<String> values = attributes.getOrDefault(key, null);
        return CollectionUtils.isEmpty(values) ? null : values.get(0);
    }

    /**
     * 将属性转换为Map形式。此方法返回attribute的快照，修改快照将不会对JobAttribute本身造成任何影响。
     * @return 属性k-v Map。
     */
    @JsonValue
    public Map<String, List<String>> toMap() {
        return new HashMap<>(attributes);
    }

    @Override
    public String toString() {
        return JacksonUtils.toJSONString(attributes);
    }
}
