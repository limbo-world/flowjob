package org.limbo.flowjob.tracker.infrastructure.worker.repositories;

import org.limbo.flowjob.tracker.commons.beans.worker.domain.WorkerMetric;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetricRepository;
import org.limbo.flowjob.tracker.dao.mybatis.WorkerMetricMapper;
import org.limbo.flowjob.tracker.dao.po.WorkerMetricPO;
import org.limbo.flowjob.tracker.infrastructure.worker.converters.WorkerMetricPoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Brozen
 * @since 2021-06-03
 */
@Repository
public class MyBatisWorkerMetricRepo implements WorkerMetricRepository {

    @Autowired
    private WorkerMetricMapper mapper;

    @Autowired
    private WorkerMetricPoConverter converter;

    /**
     * {@inheritDoc}
     * @param metric worker指标信息
     */
    @Override
    public void updateMetric(WorkerMetric metric) {
        WorkerMetricPO po = converter.convert(metric);
        // TODO
    }

    /**
     * {@inheritDoc}
     * @param workerId workerId
     * @return
     */
    @Override
    public WorkerMetric getMetric(String workerId) {
        WorkerMetricPO metric = mapper.selectById(workerId);
        return converter.reverse().convert(metric);
    }

}
