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

package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.clent.dto.WorkerRegisterDTO;
import org.limbo.flowjob.broker.api.clent.param.WorkerHeartbeatParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.application.plan.converter.WorkerConverter;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.application.plan.component.WorkerFactory;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.limbo.flowjob.broker.core.worker.metric.WorkerAvailableResource;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.common.utils.Verifies;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.net.URL;
import java.util.Optional;

/**
 * 应用层服务
 *
 * @author Brozen
 * @since 2021-07-06
 */
@Slf4j
@Service
public class WorkerService {

    @Setter(onMethod_ = @Inject)
    private WorkerRepository workerRepository;

    @Setter(onMethod_ = @Inject)
    private WorkerFactory workerFactory;

    @Setter(onMethod_ = @Inject)
    private WorkerConverter converter;


    /**
     * worker心跳
     * @param heartbeatOption 心跳参数，上报部分指标数据
     */
    public void heartbeat(WorkerHeartbeatParam heartbeatOption) {
        // 查询worker并校验
        Worker worker = workerRepository.get(heartbeatOption.getWorkerId());
        Verifies.requireNotNull(worker, "worker不存在！");

        // 更新metric
        WorkerMetric metric = worker.getMetric();
        metric.setAvailableResource(WorkerAvailableResource.from(heartbeatOption.getAvailableResource()));
        worker.updateMetric(metric);

        if (log.isDebugEnabled()) {
            log.debug("receive heartbeat from " + worker.getWorkerId());
        }

    }

    /**
     * worker注册
     * @param options 注册参数
     * @return 返回所有tracker节点信息
     */
    @Transactional(rollbackOn = Throwable.class)
    public WorkerRegisterDTO register(WorkerRegisterParam options) {
        // TODO 租户鉴权

        // 新增 or 更新 worker
        URL rpcBaseUrl = converter.toWorkerRpcBaseURL(options);
        Worker worker = Optional
                .ofNullable(workerRepository.get(options.getId()))
                .map(w -> w.updateRegisterInfo(rpcBaseUrl))
                .orElseGet(() -> this.workerFactory.newWorker(rpcBaseUrl));

        // 更新metric
        WorkerMetric metric = WorkerConverter.convertMetric(options);
        worker.updateMetric(metric);

        // 保存 worker
        workerRepository.save(worker);
        log.info("worker registered " + worker);

        // 返回tracker
        WorkerRegisterDTO registerResult = new WorkerRegisterDTO();
        registerResult.setWorkerId(worker.getWorkerId());
        // registerResult.setBrokers(trackerNode.getNodes()); FIXME 怎样得到所有的 Broker 节点信息？
        return registerResult;
    }

}
