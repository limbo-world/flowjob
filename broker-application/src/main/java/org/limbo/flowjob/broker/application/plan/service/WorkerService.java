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
import org.limbo.flowjob.broker.api.constants.enums.Protocol;
import org.limbo.flowjob.broker.application.plan.support.WorkerFactory;
import org.limbo.flowjob.broker.application.plan.converter.WorkerConverter;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.limbo.flowjob.common.utils.Verifies;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
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


    /**
     * worker心跳
     * @param option 心跳参数，上报部分指标数据
     */
    @Transactional(rollbackOn = Throwable.class)
    public void heartbeat(WorkerHeartbeatParam option) {
        // 查询worker并校验
        Worker worker = workerRepository.get(option.getWorkerId());
        Verifies.requireNotNull(worker, "worker不存在！");

        // 更新metric
        worker.heartbeat(WorkerConverter.toWorkerMetric(option, worker));
        workerRepository.save(worker);

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

        // 校验 protocol
        String protocolName = options.getUrl().getProtocol();
        Protocol protocol = Protocol.parse(protocolName);
        Verifies.verify(
                protocol != Protocol.UNKNOWN,
                "Unknown worker rpc protocol:" + protocolName
        );

        // 新增 or 更新 worker
        Worker worker = Optional
                .ofNullable(workerRepository.get(options.getId()))
                .orElseGet(() -> WorkerFactory.newWorker(options));

        // 更新注册信息
        worker.register(
                options.getUrl(),
                WorkerConverter.toWorkerTags(options),
                WorkerConverter.toWorkerExecutors(options, worker),
                WorkerConverter.toWorkerMetric(options, worker)
        );

        // 保存 worker
        workerRepository.save(worker);
        log.info("worker registered " + worker);

        // 返回tracker
        return WorkerConverter.toRegisterDTO(worker);
    }

}
