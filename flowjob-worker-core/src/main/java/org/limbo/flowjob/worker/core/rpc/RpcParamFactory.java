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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.limbo.flowjob.api.constants.ExecuteResult;
import org.limbo.flowjob.api.param.agent.SubTaskCreateParam;
import org.limbo.flowjob.api.param.agent.TaskFeedbackParam;
import org.limbo.flowjob.api.param.agent.TaskReportParam;
import org.limbo.flowjob.api.param.broker.WorkerExecutorRegisterParam;
import org.limbo.flowjob.api.param.broker.WorkerHeartbeatParam;
import org.limbo.flowjob.api.param.broker.WorkerRegisterParam;
import org.limbo.flowjob.api.param.broker.WorkerResourceParam;
import org.limbo.flowjob.worker.core.domain.SubTask;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.core.domain.WorkerResources;

import java.net.URL;
import java.util.ArrayList;
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

    public static TaskFeedbackParam taskFeedbackParam(String jobId, String taskId, Map<String, Object> context, String result, Throwable ex) {
        TaskFeedbackParam feedbackParam = new TaskFeedbackParam();
        feedbackParam.setJobId(jobId);
        feedbackParam.setTaskId(taskId);
        feedbackParam.setResult(ExecuteResult.SUCCEED);
        feedbackParam.setContext(context);
        feedbackParam.setResultData(result);

        if (ex != null) {
            feedbackParam.setResult(ExecuteResult.FAILED);
            feedbackParam.setErrorStackTrace(ExceptionUtils.getStackTrace(ex));
        }
        return feedbackParam;
    }

    public static TaskReportParam subTaskReportParam(String taskId) {
        return TaskReportParam.builder()
                .taskId(taskId)
                .build();
    }

    public static SubTaskCreateParam subTaskCreateParam(String jobId, List<SubTask> subTasks) {
        SubTaskCreateParam param = new SubTaskCreateParam();
        param.setJobId(jobId);

        List<SubTaskCreateParam.SubTaskInfoParam> subTaskParams = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(subTasks)) {
            subTaskParams = new ArrayList<>();
            for (SubTask subTask : subTasks) {
                SubTaskCreateParam.SubTaskInfoParam subTaskInfoParam = new SubTaskCreateParam.SubTaskInfoParam();
                subTaskInfoParam.setTaskId(subTask.getTaskId());
                subTaskInfoParam.setData(subTask.getAttributes());
                subTaskParams.add(subTaskInfoParam);
            }
        }
        param.setSubTasks(subTaskParams);
        return param;
    }

}
