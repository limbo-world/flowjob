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

package org.limbo.flowjob.worker.core.rpc;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.limbo.flowjob.api.remote.param.TaskFeedbackParam;
import org.limbo.flowjob.api.remote.param.WorkerExecutorRegisterParam;
import org.limbo.flowjob.api.remote.param.WorkerHeartbeatParam;
import org.limbo.flowjob.api.remote.param.WorkerRegisterParam;
import org.limbo.flowjob.api.remote.param.WorkerResourceParam;
import org.limbo.flowjob.common.constants.ExecuteResult;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.core.domain.WorkerResources;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2022/10/25
 */
public class RpcParamFactory {

    /**
     * 封装 Worker 注册参数
     */
    public static WorkerRegisterParam registerParam(Worker worker) {
        // 执行器
        List<WorkerExecutorRegisterParam> executors = worker.getExecutors().values().stream()
                .map(executor -> {
                    WorkerExecutorRegisterParam executorRegisterParam = new WorkerExecutorRegisterParam();
                    executorRegisterParam.setName(executor.getName());
                    executorRegisterParam.setDescription(executor.getDescription());
                    return executorRegisterParam;
                })
                .collect(Collectors.toList());

        // 可用资源
        WorkerResources resource = worker.getResource();
        WorkerResourceParam resourceParam = new WorkerResourceParam();
        resourceParam.setAvailableCpu(resource.availableCpu());
        resourceParam.setAvailableRAM(resource.availableRam());
        resourceParam.setAvailableQueueLimit(resource.availableQueueSize());

        // Tags
        Set<WorkerRegisterParam.Tag> tags;
        Worker.WorkerTag workerTag = worker.getTags();
        if (workerTag == null || workerTag.isEmpty()) {
            tags = new HashSet<>();
        } else {
            tags = workerTag.keySet().stream()
                    .flatMap(key -> workerTag.getOrDefault(key, Collections.emptySet())
                            .stream().map(value -> new WorkerRegisterParam.Tag(key, value))
                    )
                    .collect(Collectors.toSet());
        }

        // 组装注册参数
        URL workerRpcBaseURL = worker.getRpcBaseURL();
        WorkerRegisterParam registerParam = new WorkerRegisterParam();
        registerParam.setName(worker.getName());
        registerParam.setUrl(workerRpcBaseURL);
        registerParam.setExecutors(executors);
        registerParam.setAvailableResource(resourceParam);
        registerParam.setTags(tags);

        return registerParam;
    }

    /**
     * 封装 Worker 心跳参数
     */
    public static WorkerHeartbeatParam heartbeatParam(Worker worker) {
        // 可用资源
        WorkerResources workerResource = worker.getResource();
        WorkerResourceParam resource = new WorkerResourceParam();
        resource.setAvailableCpu(workerResource.availableCpu());
        resource.setAvailableRAM(workerResource.availableRam());
        resource.setAvailableQueueLimit(workerResource.availableQueueSize());

        // 组装心跳参数
        WorkerHeartbeatParam heartbeatParam = new WorkerHeartbeatParam();
        heartbeatParam.setAvailableResource(resource);

        return heartbeatParam;
    }

    public static TaskFeedbackParam taskFeedbackParam(Map<String, Object> context, Map<String, Object> jobAttributes, Object result, Throwable ex) {
        TaskFeedbackParam feedbackParam = new TaskFeedbackParam();
        feedbackParam.setResult(ExecuteResult.SUCCEED.result);
        feedbackParam.setContext(context);
        feedbackParam.setJobAttributes(jobAttributes);
        feedbackParam.setResultData(result);

        if (ex != null) {
            feedbackParam.setResult(ExecuteResult.FAILED.result);
            feedbackParam.setErrorStackTrace(ExceptionUtils.getStackTrace(ex));
        }
        return feedbackParam;
    }
}
