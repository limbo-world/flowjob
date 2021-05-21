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

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brozen
 * @since 2021-05-19
 */
public class SimpleJobAttribute implements JobAttributes {

    /**
     * 内部数据结构
     */
    private final Map<String, List<String>> attributes;

    public SimpleJobAttribute(Map<String, List<String>> attributes) {
        this.attributes = Collections.unmodifiableMap(new HashMap<>(attributes));
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

    @Override
    public Map<String, List<String>> toMap() {
        return new HashMap<>(attributes);
    }

}
