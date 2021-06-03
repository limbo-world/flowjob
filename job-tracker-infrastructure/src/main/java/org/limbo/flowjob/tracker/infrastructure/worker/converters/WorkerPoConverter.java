package org.limbo.flowjob.tracker.infrastructure.worker.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerProtocol;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerStatus;
import org.limbo.flowjob.tracker.core.tracker.worker.HttpWorker;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetricRepository;
import org.limbo.flowjob.tracker.core.tracker.worker.statistics.WorkerStatisticsRepository;
import org.limbo.flowjob.tracker.dao.po.WorkerPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.netty.http.client.HttpClient;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Component
public class WorkerPoConverter extends Converter<Worker, WorkerPO> {

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private WorkerMetricRepository metricRepository;

    @Autowired
    private WorkerStatisticsRepository workerStatisticsRepository;

    /**
     * 将领域对象{@link Worker}转换为持久化对象{@link WorkerPO}
     * @param _do {@link Worker}领域对象
     * @return {@link WorkerPO}持久化对象
     */
    @Override
    protected WorkerPO doForward(Worker _do) {
        WorkerPO po = new WorkerPO();
        po.setWorkerId(_do.getWorkerId());
        po.setProtocol(_do.getProtocol().protocol);
        po.setIp(_do.getIp());
        po.setPort(_do.getPort());
        po.setStatus(_do.getStatus().status);
        po.setDeleted(false);
        return po;
    }

    /**
     * 将持久化对象{@link WorkerPO}转换为领域对象{@link Worker}
     * @param po {@link WorkerPO}持久化对象
     * @return {@link Worker}领域对象
     */
    @Override
    protected Worker doBackward(WorkerPO po) {
        // 已删除则不返回
        if (po.getDeleted()) {
            return null;
        }

        // 目前只支持一种protocol
        WorkerProtocol protocol = WorkerProtocol.parse(po.getProtocol());
        if (protocol == WorkerProtocol.HTTP) {
            return convertToHttpWorker(po);
        }

        throw new IllegalArgumentException("Cannot determine worker protocol: " + po.getProtocol());
    }

    /**
     * 将持久化对象{@link WorkerPO}转换为领域对象{@link HttpWorker}
     * @param po {@link WorkerPO}持久化对象
     * @return {@link HttpWorker}领域对象
     */
    private Worker convertToHttpWorker(WorkerPO po) {

        HttpWorker worker = new HttpWorker(httpClient, workerRepository, metricRepository, workerStatisticsRepository);
        worker.setWorkerId(po.getWorkerId());
        worker.setProtocol(WorkerProtocol.parse(po.getProtocol()));
        worker.setIp(po.getIp());
        worker.setPort(po.getPort());
        worker.setStatus(WorkerStatus.parse(po.getStatus()));
        return worker;

    }
}
