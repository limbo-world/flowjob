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

package org.limbo.flowjob.broker.core.plan.job.dag;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.common.utils.Verifies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * todo @D 简单描述下 DAG 内存的存储结构？看起来是颗树。
 *
 * @author Devil
 * @since 2022/8/1
 */
public class DAG<T extends DAGNode> implements Serializable {

    private static final long serialVersionUID = 4746630152041623943L;

    private static final int STATUS_INIT = 0;

    /**
     * 当遍历的时候第二次进入某个节点，表示成环
     */
    private static final int STATUS_VISITED = 1;

    /**
     * 当一个节点 所有子节点都已经被遍历 而且没有环
     * 则它的后继判断也可以省略了
     * 因为如果要形成环，必定是会访问之前已访问的节点
     * 最简单的例子：有环列表
     */
    private static final int STATUS_FILTER = 2;

    /**
     * 根
     */
    private Set<String> roots;

    /**
     * 叶子
     */
    private Set<String> leaf;

    /**
     * 节点映射关系
     */
    private Map<String, T> nodes;

    public DAG(List<T> nodeList) {
        if (CollectionUtils.isEmpty(nodeList)) {
            return;
        }

        this.nodes = new HashMap<>();
        this.roots = new HashSet<>();
        this.leaf = new HashSet<>();

        init(nodeList);
    }


    /**
     * 根据作业列表，初始化 DAG 结构
     */
    private void init(List<T> nodeList) {
        // 数据初始化
        nodeList.forEach(node -> {
            nodes.put(node.getId(), node);
        });
        nodeList.forEach(node -> {
            if (CollectionUtils.isEmpty(node.getChildrenIds())) {
                return;
            }
            // 判断childrenId是否都有对应节点 设置父id
            for (String childrenId : node.getChildrenIds()) {
                T t = nodes.get(childrenId);
                Verifies.notNull(t, "node " + node.getId() + " child " + childrenId + " is not exist");
                t.addParent(t.getId());
            }
        });

        // 获取 根节点（没有其它节点指向的节点）叶子节点（没有子节点的）
        roots = nodeList.stream().map(T::getId).collect(Collectors.toSet());
        nodeList.forEach(node -> {
            if (CollectionUtils.isEmpty(node.getChildrenIds())) {
                leaf.add(node.getId());
            } else {
                for (String childrenId : node.getChildrenIds()) {
                    roots.remove(childrenId);
                }
            }
        });
        Verifies.notEmpty(roots, "root nodes ie empty");
        Verifies.notEmpty(leaf, "leaf nodes ie empty");

        // 是否有环
        for (String root : roots) {
            Verifies.verify(!hasCyclic(nodes.get(root)), "jobs has cyclic");
        }
    }


    /**
     * 从 DAG 中查找是否存在指定作业ID的节点，存在则返回作业信息，不存在返回null。
     */
    public T getJob(String id) {
        return nodes.get(id);
    }


    /**
     * 获取叶子节点 也就是最后执行的节点
     */
    public List<T> getLeafNodes() {
        List<T> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(leaf)) {
            return result;
        }
        for (String id : leaf) {
            result.add(nodes.get(id));
        }
        return result;
    }


    /**
     * 获取所有根节点
     */
    public List<T> roots() {
        List<T> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(roots)) {
            return result;
        }
        for (String root : roots) {
            result.add(nodes.get(root));
        }
        return result;
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
     * @param node
     * @return @D 返回的是啥？
     */
    public boolean hasCyclic(DAGNode node) {
        // 表示当前节点已被标记
        node.setStatus(STATUS_VISITED);
        // 如果不存在子节点 则表示此顶点不再有出度 返回父节点
        if (CollectionUtils.isNotEmpty(node.getChildrenIds())) {
            // 遍历子节点
            for (String childId : node.getChildrenIds()) {
                DAGNode child = nodes.get(childId);
                if (child == null || STATUS_FILTER == child.getStatus()) {
                    continue;
                }
                if (STATUS_VISITED == child.getStatus()) {
                    return true;
                }
                if (hasCyclic(child)) {
                    return true;
                }
            }
        }
        node.setStatus(STATUS_FILTER);
        return false;
    }


    /**
     * 获取 DAG 中所有节点对应的作业
     */
    public List<T> nodes() {
        return new ArrayList<>(nodes.values());
    }

}
