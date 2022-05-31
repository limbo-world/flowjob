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

package org.limbo.flowjob.tracker.admin.service.worker;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.param.worker.WorkerHeartbeatParam;
import org.limbo.flowjob.broker.core.utils.Symbol;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerAvailableResource;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetric;
import org.limbo.utils.verifies.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 应用层服务
 *
 * @author Brozen
 * @since 2021-07-06
 */
@Slf4j
@Service
public class WorkerService {

    @Autowired
    private WorkerRepository workerRepository;


    /**
     * worker心跳
     * @param heartbeatOption 心跳参数，上报部分指标数据
     */
    public Mono<Symbol> heartbeat(WorkerHeartbeatParam heartbeatOption) {
        // 查询worker并校验
        Worker worker = workerRepository.getWorker(heartbeatOption.getWorkerId());
        Verifies.notNull(worker, "worker不存在！");

        // 更新metric
        WorkerMetric metric = worker.getMetric();
        metric.setAvailableResource(WorkerAvailableResource.from(heartbeatOption.getAvailableResource()));
        worker.updateMetric(metric);

        if (log.isDebugEnabled()) {
            log.debug("receive heartbeat from " + worker.getWorkerId());
        }

        return Mono.just(Symbol.newSymbol());
    }


}
