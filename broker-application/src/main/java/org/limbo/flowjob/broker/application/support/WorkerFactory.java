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

package org.limbo.flowjob.broker.application.support;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.api.remote.param.WorkerExecutorRegisterParam;
import org.limbo.flowjob.api.remote.param.WorkerRegisterParam;
import org.limbo.flowjob.api.remote.param.WorkerResourceParam;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.executor.WorkerExecutor;
import org.limbo.flowjob.broker.core.worker.metric.WorkerAvailableResource;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.common.constants.WorkerStatus;
import org.limbo.flowjob.common.utils.UUIDUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-08-29
 */
public class WorkerFactory {

    /**
     * 生成新的worker，根据注册参数创建
     *
     * @param options worker 注册参数
     * @return worker领域对象
     */
    public static Worker newWorker(String workerId, WorkerRegisterParam options) {
        String name = StringUtils.isNotBlank(options.getName()) ? options.getName() : UUIDUtils.randomID();
        return Worker.builder()
                .id(workerId)
                .name(name)
                .rpcBaseUrl(options.getUrl())
                .executors(executors(options.getExecutors()))
                .tags(Maps.newHashMap())
                .metric(metric(Collections.emptyList(), options.getAvailableResource()))
                .status(WorkerStatus.TERMINATED)
                .build();
    }


    private static List<WorkerExecutor> executors(List<WorkerExecutorRegisterParam> executorsParam) {
        if (CollectionUtils.isEmpty(executorsParam)) {
            return Collections.emptyList();
        }
        return executorsParam.stream().map(param -> new WorkerExecutor(param.getName(), param.getDescription())).collect(Collectors.toList());
    }

    private static WorkerMetric metric(List<String> executingJobs, WorkerResourceParam availableResource) {
        return new WorkerMetric(
                executingJobs,
                new WorkerAvailableResource(
                        availableResource.getAvailableCpu(),
                        availableResource.getAvailableRAM(),
                        availableResource.getAvailableQueueLimit()
                ),
                TimeUtils.currentLocalDateTime()
        );
    }

}
