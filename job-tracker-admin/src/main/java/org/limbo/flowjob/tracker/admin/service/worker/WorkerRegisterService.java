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

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.constants.enums.WorkerProtocol;
import org.limbo.flowjob.broker.api.constants.enums.WorkerStatus;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerExecutorRegisterDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterResult;
import org.limbo.flowjob.tracker.core.tracker.TrackerNode;
import org.limbo.flowjob.tracker.core.tracker.worker.HttpWorker;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerAvailableResource;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerExecutor;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetric;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetricRepository;
import org.limbo.flowjob.tracker.core.tracker.worker.statistics.WorkerStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用服务层：worker注册操作
 *
 * @author Brozen
 * @since 2021-06-03
 */
@Slf4j
@Service
public class WorkerRegisterService {

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private WorkerMetricRepository metricRepository;

    @Autowired
    private WorkerStatisticsRepository statisticsRepository;

    @Autowired
    private TrackerNode trackerNode;

    /**
     * worker注册
     * @param options 注册参数
     * @return 返回所有tracker节点信息
     */
    @Transactional(rollbackFor = Throwable.class)
    public Mono<WorkerRegisterResult> register(WorkerRegisterOptionDto options) {

        // TODO 租户鉴权

        // 新增 or 更新 worker
        Worker worker = workerRepository.getWorker(options.getId());
        if (worker == null) {

            worker = createNewWorker(options);
            workerRepository.addWorker(worker);

        } else {

            worker.setHost(options.getHost());
            worker.setPort(options.getPort());
            worker.setProtocol(options.getProtocol());
            worker.setStatus(WorkerStatus.RUNNING);
            workerRepository.updateWorker(worker);

        }

        // 更新metric
        WorkerMetric metric = createMetric(options);
        worker.updateMetric(metric);

        log.info("worker registered " + worker);

        // 返回tracker
        WorkerRegisterResult registerResult = new WorkerRegisterResult();
        registerResult.setWorkerId(worker.getWorkerId());
        registerResult.setTrackers(trackerNode.getNodes());
        return Mono.just(registerResult);
    }

    /**
     * 生成新的worker，根据协议创建不同的worker
     * @param options 注册参数
     * @return worker领域对象
     */
    @Nonnull
    private Worker createNewWorker(WorkerRegisterOptionDto options) {
        // 目前只支持HTTP协议的worker
        Worker worker;
        WorkerProtocol protocol = options.getProtocol();
        if (protocol == WorkerProtocol.HTTP) {
            worker = new HttpWorker(httpClient, workerRepository, metricRepository, statisticsRepository);
        } else {
            throw new UnsupportedOperationException("不支持的worker协议：" + protocol);
        }

        worker.setWorkerId(options.getId());
        worker.setHost(options.getHost());
        worker.setPort(options.getPort());
        worker.setProtocol(protocol);
        worker.setStatus(WorkerStatus.RUNNING);

        return worker;
    }


    /**
     * 根据注册参数，生成worker指标信息
     * @param options worker注册参数
     * @return worker指标领域对象
     */
    private WorkerMetric createMetric(WorkerRegisterOptionDto options) {
        WorkerMetric metric = new WorkerMetric();
        metric.setExecutors(convertWorkerExecutor(options));
        metric.setExecutingJobs(Lists.newArrayList()); // TODO 是否需要记录？
        metric.setAvailableResource(WorkerAvailableResource.from(options.getAvailableResource()));
        return metric;
    }


    /**
     * {@link WorkerExecutorRegisterDto} => {@link WorkerExecutor} 列表转换，根据注册参数中的id设置workerId
     */
    private List<WorkerExecutor> convertWorkerExecutor(WorkerRegisterOptionDto options) {
        List<WorkerExecutor> executors;
        if (CollectionUtils.isNotEmpty(options.getJobExecutors())) {
            executors = options.getJobExecutors().stream()
                    .map(this::convertWorkerExecutor)
                    .peek(exe -> exe.setWorkerId(options.getId()))
                    .collect(Collectors.toList());
        } else {
            executors = Lists.newArrayList();
        }

        return executors;
    }


    /**
     * {@link WorkerExecutorRegisterDto} => {@link WorkerExecutor}
     */
    private WorkerExecutor convertWorkerExecutor(WorkerExecutorRegisterDto dto) {
        WorkerExecutor executor = new WorkerExecutor();
        executor.setName(dto.getName());
        executor.setDescription(dto.getDescription());
        executor.setType(dto.getType());
        return executor;
    }

}
