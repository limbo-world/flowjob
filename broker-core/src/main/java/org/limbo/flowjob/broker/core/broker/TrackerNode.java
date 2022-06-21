package org.limbo.flowjob.broker.core.broker;

import org.limbo.flowjob.broker.api.dto.BrokerDTO;

import java.util.List;

/**
 *
 * 作业分发节点抽象。
 *
 * tracker节点
 *
 * @author Devil
 * @since 2021/8/4
 */
public interface TrackerNode {

    /**
     * 启动JobTracker
     * @return 返回可用于关闭JobTracker的Disposable
     */
    DisposableTrackerNode start();

    /**
     * 停止JobTracker
     */
    void stop();

    /**
     * @return JobTracker是否正在运行
     */
    boolean isRunning();

    /**
     * @return JobTracker是否已停止
     */
    boolean isStopped();

    JobTracker jobTracker();

    /**
     * 返回可用节点
     * @return
     */
    List<BrokerDTO> getNodes();
    /**
     * 获取此JobTracker的生命周期监听注册器。
     * @return 生命周期监听注册器
     */
    TrackerNodeLifecycle lifecycle();

}
