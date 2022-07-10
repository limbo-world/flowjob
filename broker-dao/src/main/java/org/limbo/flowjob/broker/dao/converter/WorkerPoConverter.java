/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.dao.converter;

import com.google.common.base.Converter;
import org.limbo.flowjob.broker.api.constants.enums.WorkerProtocol;
import org.limbo.flowjob.broker.api.constants.enums.WorkerStatus;
import org.limbo.flowjob.broker.core.worker.HttpWorker;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetricRepository;
import org.limbo.flowjob.broker.core.worker.statistics.WorkerStatisticsRepository;
import org.limbo.flowjob.broker.dao.entity.WorkerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.netty.http.client.HttpClient;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Component
public class WorkerPoConverter extends Converter<Worker, WorkerEntity> {

//    @Autowired
    private HttpClient httpClient;

    @Lazy
    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private WorkerMetricRepository metricRepository;

    @Autowired
    private WorkerStatisticsRepository workerStatisticsRepository;

    /**
     * 将领域对象{@link Worker}转换为持久化对象{@link WorkerEntity}
     * @param _do {@link Worker}领域对象
     * @return {@link WorkerEntity}持久化对象
     */
    @Override
    protected WorkerEntity doForward(Worker _do) {
        WorkerEntity po = new WorkerEntity();
        po.setId(Long.valueOf(_do.getWorkerId()));
        po.setProtocol(_do.getProtocol().protocol);
        po.setHost(_do.getHost());
        po.setPort(_do.getPort());
        po.setStatus(_do.getStatus().status);
        po.setDeleted(false);
        return po;
    }

    /**
     * 将持久化对象{@link WorkerEntity}转换为领域对象{@link Worker}
     * @param po {@link WorkerEntity}持久化对象
     * @return {@link Worker}领域对象
     */
    @Override
    protected Worker doBackward(WorkerEntity po) {
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
     * 将持久化对象{@link WorkerEntity}转换为领域对象{@link HttpWorker}
     * @param po {@link WorkerEntity}持久化对象
     * @return {@link HttpWorker}领域对象
     */
    private Worker convertToHttpWorker(WorkerEntity po) {

        HttpWorker worker = new HttpWorker(httpClient, workerRepository, metricRepository, workerStatisticsRepository);
        worker.setProtocol(WorkerProtocol.parse(po.getProtocol()));
        worker.setHost(po.getHost());
        worker.setPort(po.getPort());
        worker.setStatus(WorkerStatus.parse(po.getStatus()));
        return worker;

    }
}
