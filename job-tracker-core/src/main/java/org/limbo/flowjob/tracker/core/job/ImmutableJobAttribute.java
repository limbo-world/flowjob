package org.limbo.flowjob.tracker.core.job;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brozen
 * @since 2021-05-21
 */
public class ImmutableJobAttribute implements JobAttributes {

    /**
     * 内部数据结构
     */
    private final Map<String, List<String>> attributes;

    public ImmutableJobAttribute(Map<String, List<String>> attributes) {
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

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Map<String, List<String>> toMap() {
        return new HashMap<>(attributes);
    }

}
