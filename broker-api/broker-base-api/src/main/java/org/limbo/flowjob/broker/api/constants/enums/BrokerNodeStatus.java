package org.limbo.flowjob.broker.api.constants.enums;

/**
 * @author Devil
 * @since 2021/8/12
 */
public enum BrokerNodeStatus {
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
    VOTING,
}
