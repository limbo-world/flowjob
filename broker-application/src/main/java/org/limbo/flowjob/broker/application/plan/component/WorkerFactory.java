/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.application.plan.component;

import lombok.Setter;
import org.limbo.flowjob.broker.api.constants.enums.WorkerStatus;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetricRepository;
import org.limbo.flowjob.broker.core.worker.statistics.WorkerStatisticsRepository;
import org.limbo.flowjob.common.utils.UUIDUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.net.URL;

/**
 * @author Brozen
 * @since 2022-08-29
 */
@Component
public class WorkerFactory {

    @Setter(onMethod_ = @Inject)
    private WorkerRepository workerRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerMetricRepository workerMetricRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerStatisticsRepository workerStatisticsRepo;

    /**
     * 生成新的worker，根据注册参数创建
     * @param rpcBaseUrl RPC 通信的基础 URL
     * @return worker领域对象
     */
    public Worker newWorker(URL rpcBaseUrl) {
        return Worker.builder()
                .workerRepository(workerRepo)
                .metricRepository(workerMetricRepo)
                .statisticsRepository(workerStatisticsRepo)
                .workerId(UUIDUtils.randomID()) // FIXME 这里要不要考虑 worker 注册时指定 ID 的情况？
                .rpcBaseUrl(rpcBaseUrl)
                .status(WorkerStatus.RUNNING)
                .build();
    }


}
