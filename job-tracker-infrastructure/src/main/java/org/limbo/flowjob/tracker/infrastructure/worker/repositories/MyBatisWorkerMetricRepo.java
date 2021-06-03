package org.limbo.flowjob.tracker.infrastructure.worker.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetric;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetricRepository;
import org.limbo.flowjob.tracker.dao.mybatis.WorkerMetricMapper;
import org.limbo.flowjob.tracker.dao.po.WorkerMetricPO;
import org.limbo.flowjob.tracker.infrastructure.worker.converters.WorkerMetricPoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Objects;

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
        Objects.requireNonNull(po);

        int effected = mapper.update(po, Wrappers.<WorkerMetricPO>lambdaUpdate()
                .eq(WorkerMetricPO::getWorkerId, po.getWorkerId()));
        if (effected <= 0) {

            effected = mapper.insertIgnore(po);
            if (effected != 1) {
                throw new IllegalStateException(String.format("Update worker error, effected %s rows", effected));
            }
        }
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
