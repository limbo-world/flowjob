package org.limbo.flowjob.tracker.core.job.dag;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Map;

/**
 * @author Devil
 * @since 2021/8/18
 */
public class DAG {

    private static final int STATE_INIT = 0;
    /**
     * 当遍历的时候第二次进入某个节点，表示成环
     */
    private static final int STATE_VISITED = 1;
    /**
     * 当一个节点 所有子节点都已经被遍历 而且没有环
     * 则它的后继判断也可以省略了
     * 因为如果要形成环，必定是会访问之前已访问的节点
     * 最简单的例子：有环列表
     */
    private static final int STATE_FILTER = 2;

    /**
     * 深度优先搜索
     * @param node
     * @param nodes
     * @return
     */
    public static boolean hasCyclic(DAGNode node, Map<String, DAGNode> nodes) {
        // 表示当前节点已被标记
        node.setState(STATE_VISITED);
        // 如果不存在子节点 则表示此顶点不再有出度 返回父节点
        if (CollectionUtils.isNotEmpty(node.getChildrenIds())) {
            // 遍历子节点
            for (String childId : node.getChildrenIds()) {
                DAGNode child = nodes.get(childId);
                if (child == null || STATE_FILTER == child.getState()) {
                    continue;
                }
                if (STATE_VISITED == child.getState()) {
                    return true;
                }
                if (hasCyclic(child, nodes)) {
                    return true;
                }
            }
        }
        node.setState(STATE_FILTER);
        return false;
    }

}
