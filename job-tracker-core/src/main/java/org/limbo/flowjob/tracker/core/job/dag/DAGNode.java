package org.limbo.flowjob.tracker.core.job.dag;

import lombok.Data;

import java.util.List;

/**
 * @author Devil
 * @since 2021/8/18
 */
@Data
public class DAGNode {

    private String id;

    private List<String> childrenIds;
    /**
     * 状态 0 初始-未访问 1 已访问
     */
    private int state;

    public DAGNode(String id, List<String> childrenIds) {
        this.id = id;
        this.childrenIds = childrenIds;
    }

}
