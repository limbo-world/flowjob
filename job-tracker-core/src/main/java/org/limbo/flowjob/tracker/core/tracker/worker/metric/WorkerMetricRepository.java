package org.limbo.flowjob.tracker.core.tracker.worker.metric;

/**
 * @author Brozen
 * @since 2021-06-02
 */
public interface WorkerMetricRepository {

    /**
     * 更新worker指标信息
     * @param metric worker指标信息
     */
    void updateMetric(WorkerMetric metric);

    /**
     * 根据worker节点ID查询节点的指标信息
     * @param workerId workerId
     * @return worker节点的指标信息
     */
    WorkerMetric getMetric(String workerId);

}
