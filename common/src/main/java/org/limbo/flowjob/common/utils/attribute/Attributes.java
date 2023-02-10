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

package org.limbo.flowjob.common.utils.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 属性。k-v映射，一个key可以对应多个value
 *
 * @author Brozen
 * @since 2021-05-21
 */
public class Attributes implements Serializable {

    private static final long serialVersionUID = -8346798332193684079L;

    /**
     * 内部数据结构
     */
    @JsonValue
    protected Map<String, Object> attributes;

    public Attributes() {
        this.attributes = new HashMap<>();
    }

    public Attributes(String attr) {
        if (StringUtils.isBlank(attr)) {
            this.attributes = new HashMap<>();
        } else {
            this.attributes = JacksonUtils.parseObject(attr, new TypeReference<Map<String, Object>>() {
            });
        }
    }

    @JsonCreator
    public Attributes(Map<String, Object> attributes) {
        this.attributes = attributes == null ? new HashMap<>() : new HashMap<>(attributes);
    }

    /**
     * 根据key获取value值
     * @param key 属性key
     * @return 所有属性values
     */
    public Object get(String key) {
        return attributes.get(key);
    }


    /**
     * 将属性转换为Map形式。此方法返回attribute的快照，修改快照将不会对JobAttribute本身造成任何影响。
     * @return 属性k-v Map。
     */
    public Map<String, Object> toMap() {
        return new HashMap<>(attributes);
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public void put(Attributes attr) {
        if (attr == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : attr.toMap().entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 设置属性
     * @param key 属性key
     * @param value 属性value
     */
    public void put(String key, Object value) {
        attributes.putIfAbsent(key, value);
    }

    @Override
    public String toString() {
        return JacksonUtils.toJSONString(attributes);
    }
}
