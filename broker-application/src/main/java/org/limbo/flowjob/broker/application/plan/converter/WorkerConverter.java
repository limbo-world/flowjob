/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.application.plan.converter;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.dto.WorkerRegisterDTO;
import org.limbo.flowjob.api.param.WorkerExecutorRegisterParam;
import org.limbo.flowjob.api.param.WorkerHeartbeatParam;
import org.limbo.flowjob.api.param.WorkerRegisterParam;
import org.limbo.flowjob.api.param.WorkerResourceParam;
import org.limbo.flowjob.api.dto.BrokerDTO;
import org.limbo.flowjob.api.dto.BrokerTopologyDTO;
import org.limbo.flowjob.broker.core.cluster.Node;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.executor.WorkerExecutor;
import org.limbo.flowjob.broker.core.worker.metric.WorkerAvailableResource;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-08-12
 */
@Slf4j
public class WorkerConverter {


    /**
     * 根据注册参数，生成 worker tag 信息
     * @param options worker 注册参数
     * @return worker tags
     */
    public static Map<String, List<String>> toWorkerTags(WorkerRegisterParam options) {
        return options.getTags().stream().reduce(
                new ConcurrentHashMap<>(),
                (map, tag) -> {
                    List<String> values = map.computeIfAbsent(tag.getKey(), k -> new ArrayList<>());
                    values.add(tag.getValue());
                    return map;
                },
                (m1, m2) -> {
                    m2.putAll(m1);
                    return m2;
                }
        );
    }


    /**
     * 根据注册参数，生成worker指标信息
     * @param options worker注册参数
     */
    public static WorkerMetric toWorkerMetric(WorkerRegisterParam options) {
        WorkerResourceParam resource = options.getAvailableResource();
        return WorkerMetric.builder()
                .executingJobs(Lists.newArrayList()) // TODO 是否需要记录执行中的作业？
                .availableResource(new WorkerAvailableResource(
                        resource.getAvailableCpu(),
                        resource.getAvailableRAM(),
                        resource.getAvailableQueueLimit()
                ))
                .build();
    }


    /**
     * 根据心跳参数，生成worker指标信息
     * @param options worker注册参数
     */
    public static WorkerMetric toWorkerMetric(WorkerHeartbeatParam options) {
        WorkerResourceParam resource = options.getAvailableResource();
        return WorkerMetric.builder()
                .executingJobs(Lists.newArrayList()) // TODO 是否需要记录？
                .availableResource(new WorkerAvailableResource(
                        resource.getAvailableCpu(),
                        resource.getAvailableRAM(),
                        resource.getAvailableQueueLimit()
                ))
                .build();
    }


    /**
     * Worker 执行器列表转换，根据注册参数中的 id 设置 workerId
     * {@link WorkerExecutorRegisterParam} => {@link WorkerExecutor}
     */
    public static List<WorkerExecutor> toWorkerExecutors(WorkerRegisterParam options) {
        List<WorkerExecutor> executors;
        if (CollectionUtils.isNotEmpty(options.getExecutors())) {
            executors = options.getExecutors().stream()
                    .map(WorkerConverter::toWorkerExecutor)
                    .collect(Collectors.toList());
        } else {
            executors = Lists.newArrayList();
        }

        return executors;
    }


    /**
     * {@link WorkerExecutorRegisterParam} => {@link WorkerExecutor}
     */
    public static WorkerExecutor toWorkerExecutor(WorkerExecutorRegisterParam dto) {
        return WorkerExecutor.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }


    /**
     * Worker 注册结果
     */
    public static WorkerRegisterDTO toRegisterDTO(Worker worker, Collection<Node> nodes) {
        WorkerRegisterDTO registerResult = new WorkerRegisterDTO();
        registerResult.setWorkerId(worker.getId());
        registerResult.setBrokerTopology(toBrokerTopologyDTO(nodes));
        return registerResult;
    }

    public static BrokerTopologyDTO toBrokerTopologyDTO(Collection<Node> nodes) {
        BrokerTopologyDTO brokerTopologyDTO = new BrokerTopologyDTO();
        if (CollectionUtils.isNotEmpty(nodes)) {
            for (Node node : nodes) {
                brokerTopologyDTO.getBrokers().add(new BrokerDTO(node.getHost(), node.getPort()));
            }
        }
        return brokerTopologyDTO;
    }

}
