package org.limbo.flowjob.tracker.core.tracker;

/**
 * @author Devil
 * @since 2021/8/12
 */
public enum NodeState {
    /**
     * 初始化
     */
    INIT,
    /**
     * 启动中
     */
    STARTING,
    /**
     * 启动完成，可以提供服务
     */
    STARTED,
    /**
     * 停止中
     */
    STOPPING,
    /**
     * 停止完成
     */
    TERMINATED,

    /**
     * 选举中
     */
    VOTEING,
}
