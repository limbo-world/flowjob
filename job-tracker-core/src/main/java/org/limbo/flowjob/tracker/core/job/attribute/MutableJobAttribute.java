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

package org.limbo.flowjob.tracker.core.job.attribute;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.beans.domain.job.JobAttributes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Brozen
 * @since 2021-05-19
 */
public class MutableJobAttribute extends JobAttributes {

    /**
     * 内部数据结构
     */
    private final Map<String, List<String>> attributes;

    public MutableJobAttribute(JobAttributes jobAttributes) {
        this(jobAttributes.toMap());
    }

    public MutableJobAttribute(Map<String, List<String>> attributes) {
        super(null);
        this.attributes = new ConcurrentHashMap<>(attributes);
    }

    /**
     * {@inheritDoc}
     * @param key 属性key
     * @return
     */
    @Override
    public List<String> get(String key) {
        return attributes.get(key);
    }

    /**
     * {@inheritDoc}
     * @param key 属性key
     * @return
     */
    @Override
    public String getFirst(String key) {
        List<String> values = attributes.getOrDefault(key, null);
        return CollectionUtils.isEmpty(values) ? null : values.get(0);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Map<String, List<String>> toMap() {
        return new HashMap<>(attributes);
    }

    /**
     * 设置属性
     * @param key 属性key
     * @param value 属性value
     */
    public void put(String key, String value) {
        attributes.computeIfAbsent(key, _k -> new LinkedList<>()).add(value);
    }


    /**
     * 移除属性，并返回属性key关联的values。
     * @param key 属性key。
     * @return 被删除属性key关联的values。
     */
    public List<String> remove(String key) {
        return attributes.remove(key);
    }


}
