package org.limbo.flowjob.tracker.infrastructure.worker.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetric;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.JobDescription;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerAvailableResource;
import org.limbo.flowjob.tracker.dao.po.WorkerMetricPO;
import org.limbo.utils.JacksonUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author Brozen
 * @since 2021-06-03
 */
@Component
public class WorkerMetricPoConverter extends Converter<WorkerMetric, WorkerMetricPO> {

    /**
     * 将{@link WorkerMetric}值对象转换为{@link WorkerMetricPO}持久化对象
     * @param vo {@link WorkerMetric}值对象
     * @return {@link WorkerMetricPO}持久化对象
     */
    @Override
    protected WorkerMetricPO doForward(WorkerMetric vo) {
        WorkerMetricPO po = new WorkerMetricPO();
        po.setWorkerId(vo.getWorkerId());

        WorkerAvailableResource availableResource = vo.getAvailableResource();
        po.setAvailableCpu(availableResource.getAvailableCpu());
        po.setAvailableRam(availableResource.getAvailableRam());

        // 执行中的任务
        po.setExecutingJobs(JacksonUtils.toJSONString(vo.getExecutingJobs()));

        // 设置上报时间
        Instant ts = Instant.ofEpochMilli(vo.getTimestamp());
        po.setUpdatedAt(LocalDateTime.ofInstant(ts, ZoneId.systemDefault()));

        return po;
    }

    /**
     * 将{@link WorkerMetricPO}持久化对象转换为{@link WorkerMetric}值对象
     * @param po {@link WorkerMetricPO}持久化对象
     * @return {@link WorkerMetric}值对象
     */
    @Override
    protected WorkerMetric doBackward(WorkerMetricPO po) {
        WorkerMetric metric = new WorkerMetric();
        metric.setWorkerId(po.getWorkerId());
        metric.setAvailableResource(new WorkerAvailableResource(
                po.getAvailableCpu(), po.getAvailableRam()
        ));
        metric.setExecutingJobs(JacksonUtils.parseObject(po.getExecutingJobs(), new TypeReference<List<JobDescription>>() {
        }));
        metric.setTimestamp(po.getUpdatedAt()
                .toInstant(ZoneOffset.of(ZoneId.systemDefault().getId()))
                .toEpochMilli());

        return metric;
    }
}
