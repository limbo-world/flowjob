package org.limbo.flowjob.tracker.core.job.context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作业属性，值对象。k-v映射，一个key可以对应多个value
 *
 * @author Brozen
 * @since 2021-05-21
 */
public class JobAttributes {

    /**
     * 内部数据结构
     */
    private final Map<String, List<String>> attributes;

    @JsonCreator
    public JobAttributes(Map<String, List<String>> attributes) {
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

}
