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

package org.limbo.flowjob.common.utils.dag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2022/8/1
 */
@Slf4j
//@JsonSerialize(using = DagSerializer.class)
//@JsonDeserialize(using = DagDeserializer.class)
@ToString
public class DAG<T extends DAGNode> implements Serializable {

    private static final long serialVersionUID = 4746630152041623943L;

    public static final int STATUS_INIT = 0;

    /**
     * 当遍历的时候第二次进入某个节点，表示成环
     */
    public static final int STATUS_VISITED = 1;

    /**
     * 当一个节点 所有子节点都已经被遍历 而且没有环
     * 则它的后继判断也可以省略了
     * 因为如果要形成环，必定是会访问之前已访问的节点
     * 最简单的例子：有环列表
     */
    public static final int STATUS_FILTER = 2;

    /**
     * 起始节点
     */
    private Set<String> origins;

    /**
     * 末尾节点
     */
    private Set<String> lasts;

    /**
     * 节点映射关系
     */
    private Map<String, T> nodes;

    public DAG(List<T> nodeList) {
        if (CollectionUtils.isEmpty(nodeList)) {
            return;
        }

        this.nodes = new HashMap<>();
        this.origins = new HashSet<>();
        this.lasts = new HashSet<>();

        init(nodeList);
    }


    /**
     * 根据作业列表，初始化 DAG 结构
     */
    private void init(List<T> nodeList) {
        // 数据初始化
        nodeList.forEach(node -> nodes.put(node.getId(), node));
        nodeList.forEach(node -> {
            if (CollectionUtils.isEmpty(node.getChildrenIds())) {
                return;
            }
            // 判断childrenId是否都有对应节点 设置父id
            for (String childrenId : node.getChildrenIds()) {
                T t = nodes.get(childrenId);
                if (t == null) {
                    throw new IllegalArgumentException("node " + node.getId() + " child " + childrenId + " is not exist");
                }
                t.getParentIds().add(node.getId());
            }
        });

        // 获取 根节点（没有其它节点指向的节点）叶子节点（没有子节点的）
        origins = nodeList.stream().map(T::getId).collect(Collectors.toSet());
        nodeList.forEach(node -> {
            if (CollectionUtils.isEmpty(node.getChildrenIds())) {
                lasts.add(node.getId());
            } else {
                for (String childrenId : node.getChildrenIds()) {
                    origins.remove(childrenId);
                }
            }
        });
        if (CollectionUtils.isEmpty(origins)) {
            throw new IllegalArgumentException("root nodes ie empty");
        }
        if (CollectionUtils.isEmpty(lasts)) {
            throw new IllegalArgumentException("leaf nodes ie empty");
        }

        // 是否有环
        Map<String, Integer> nodeStatuesMap = new HashMap<>();
        for (String root : origins) {
            if (hasCyclic(nodes.get(root), nodeStatuesMap)) {
                throw new IllegalArgumentException("jobs has cyclic");
            }
        }
    }


    /**
     * 从 DAG 中查找是否存在指定的节点，存在则返回作业信息，不存在返回null。
     */
    public T getNode(String id) {
        return nodes.get(id);
    }


    /**
     * 获取叶子节点 也就是最后执行的节点
     */
    public List<T> lasts() {
        return lasts.stream().map(nodeId -> nodes.get(nodeId)).collect(Collectors.toList());
    }


    /**
     * 获取所有根节点
     */
    public List<T> origins() {
        return origins.stream().map(nodeId -> nodes.get(nodeId)).collect(Collectors.toList());
    }


    /**
     * 获取后续节点
     */
    public List<T> subNodes(String id) {
        List<T> result = new ArrayList<>();
        T node = nodes.get(id);
        if (CollectionUtils.isEmpty(node.getChildrenIds())) {
            return result;
        }
        for (String childrenId : node.getChildrenIds()) {
            result.add(nodes.get(childrenId));
        }
        return result;
    }


    /**
     * 获取前置节点
     */
    public List<T> preNodes(String id) {
        List<T> result = new ArrayList<>();
        DAGNode node = nodes.get(id);
        if (CollectionUtils.isEmpty(node.getParentIds())) {
            return result;
        }
        for (String parentId : node.getParentIds()) {
            result.add(nodes.get(parentId));
        }
        return result;
    }


    /**
     * 深度优先搜索
     *
     * @param node 搜索出发节点
     * @return 如果有环返回true
     */
    public boolean hasCyclic(DAGNode node, Map<String, Integer> nodeStatuesMap) {
        // 表示当前节点已被标记
        nodeStatuesMap.put(node.getId(), STATUS_VISITED);
        // 如果不存在子节点 则表示此顶点不再有出度 返回父节点
        if (CollectionUtils.isNotEmpty(node.getChildrenIds())) {
            // 遍历子节点
            for (String childId : node.getChildrenIds()) {
                DAGNode child = nodes.get(childId);
                if (child == null || Objects.equals(STATUS_FILTER, nodeStatuesMap.get(childId))) {
                    continue;
                }
                if (Objects.equals(STATUS_VISITED, nodeStatuesMap.get(childId))) {
                    return true;
                }
                if (hasCyclic(child, nodeStatuesMap)) {
                    return true;
                }
            }
        }
        nodeStatuesMap.put(node.getId(), STATUS_FILTER);
        return false;
    }

    /**
     * 获取 DAG 中所有节点对应的作业
     */
    public List<T> nodes() {
        return new ArrayList<>(nodes.values());
    }

    private static final ObjectMapper mapper = newObjectMapper();

    private static ObjectMapper newObjectMapper() {
        ObjectMapper mapper = JacksonUtils.newObjectMapper();
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            private static final long serialVersionUID = -4928328580085816946L;

            @Override
            public boolean hasIgnoreMarker(AnnotatedMember m) {
                DAGNodeIgnoreField ann = _findAnnotation(m, DAGNodeIgnoreField.class);
                return ann != null;
            }
        });
        return mapper;
    }

    /**
     * 返回json字符串
     */
    public String json() throws JsonProcessingException {
        return mapper.writeValueAsString(nodes.values());
    }


}
