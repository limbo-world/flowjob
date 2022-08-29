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
import org.limbo.flowjob.broker.api.clent.param.WorkerExecutorRegisterParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.api.constants.enums.WorkerProtocol;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.metric.WorkerAvailableResource;
import org.limbo.flowjob.broker.core.worker.metric.WorkerExecutor;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.common.exception.VerifyException;
import org.limbo.flowjob.common.utils.Verifies;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-08-12
 */
@Slf4j
@Component
public class WorkerConverter {


    /**
     * 注册参数中解析 Worker RPC 通信的基础 URL
     */
    public URL toWorkerRpcBaseURL(WorkerRegisterParam options) {
        try {
            WorkerProtocol protocol = WorkerProtocol.parse(options.getProtocol());
            Verifies.verify(protocol != WorkerProtocol.UNKNOWN,
                    "Invalid register param: protocol=" + options.getProtocol());

            return UriComponentsBuilder.newInstance()
                    .scheme(options.getProtocol())
                    .host(options.getHost())
                    .port(options.getPort())
                    .build().toUri().toURL();
        } catch (MalformedURLException e) {
            log.error("Invalid register param：protocol={} host={} port={}",
                    options.getProtocol(), options.getHost(), options.getPort());
            throw new VerifyException("Invalid register param", e);
        }
    }


    /**
     * 根据注册参数，生成worker指标信息
     * @param options worker注册参数
     * @return worker指标领域对象
     */
    public static WorkerMetric convertMetric(WorkerRegisterParam options) {
        WorkerMetric metric = new WorkerMetric();
        metric.setExecutingJobs(Lists.newArrayList()); // TODO 是否需要记录？
        metric.setAvailableResource(WorkerAvailableResource.from(options.getAvailableResource()));
        return metric;
    }




    /**
     * Worker 执行器列表转换，根据注册参数中的 id 设置 workerId
     * {@link WorkerExecutorRegisterParam} => {@link WorkerExecutor}
     */
    public static List<WorkerExecutor> convertWorkerExecutors(WorkerRegisterParam options, Worker worker) {
        List<WorkerExecutor> executors;
        if (CollectionUtils.isNotEmpty(options.getExecutors())) {
            executors = options.getExecutors().stream()
                    .map(WorkerConverter::convertWorkerExecutor)
//                    .peek(exe -> exe.setWorkerId(options.getId())) // todo
                    .collect(Collectors.toList());
        } else {
            executors = Lists.newArrayList();
        }

        return executors;
    }


    /**
     * {@link WorkerExecutorRegisterParam} => {@link WorkerExecutor}
     */
    public static WorkerExecutor convertWorkerExecutor(WorkerExecutorRegisterParam dto) {
        WorkerExecutor executor = new WorkerExecutor();
        executor.setName(dto.getName());
        executor.setDescription(dto.getDescription());
        executor.setType(dto.getType());
        return executor;
    }



}
