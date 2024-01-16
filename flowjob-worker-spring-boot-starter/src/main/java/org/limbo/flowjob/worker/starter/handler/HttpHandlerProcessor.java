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

package org.limbo.flowjob.worker.starter.handler;

import io.netty.handler.codec.http.HttpMethod;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.api.constants.TaskType;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.param.worker.TaskSubmitParam;
import org.limbo.flowjob.common.rpc.IHttpHandlerProcessor;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.domain.Worker;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.limbo.flowjob.api.constants.rpc.HttpWorkerApi.API_SUBMIT_TASK;

/**
 * @author Devil
 * @since 2023/8/10
 */
@Slf4j
public class HttpHandlerProcessor implements IHttpHandlerProcessor {

    @Setter
    private Worker worker;

    @Override
    public String process(HttpMethod httpMethod, String uri, String data) {
        return JacksonUtils.toJSONString(process0(httpMethod, uri, data));
    }

    private ResponseDTO<?> process0(HttpMethod httpMethod, String uri, String data) {
        if (StringUtils.isBlank(uri)) {
            String msg = "Invalid request, Uri is empty.";
            return ResponseDTO.<Void>builder().notFound(msg).build();
        }

        if (HttpMethod.POST != httpMethod) {
            String msg = "Invalid request, Only POST support.";
            log.info(msg + " uri={}", uri);
            return ResponseDTO.<Void>builder().badRequest(msg).build();
        }

        try {
            if (API_SUBMIT_TASK.equals(uri)) {
                TaskSubmitParam param = JacksonUtils.parseObject(data, TaskSubmitParam.class);
                return ResponseDTO.<Boolean>builder().ok(receive(param)).build();
            }

            String msg = "Invalid request, Uri NotFound.";
            log.info(msg + " uri={}", uri);
            return ResponseDTO.<Void>builder().notFound(msg).build();
        } catch (Exception e) {
            log.error("Request process fail uri={}", uri, e);
            return ResponseDTO.<Void>builder().error(e.getMessage()).build();
        }
    }


    public Boolean receive(TaskSubmitParam param) {
        log.info("receive task {}", param);
        try {
            Task task = toTask(param);
            worker.receiveTask(task);
            return true;
        } catch (Exception e) {
            log.error("Failed to receive task:{}", JacksonUtils.toJSONString(param), e);
            return false;
        }
    }

    /**
     * Task 提交参数转为 Task
     */
    @SuppressWarnings("unchecked")
    public static Task toTask(TaskSubmitParam param) {
        TaskType taskType = TaskType.parse(param.getType());
        Task task = new Task();
        task.setJobId(param.getJobId());
        task.setTaskId(param.getTaskId());
        task.setType(taskType);
        task.setRpcUrl(param.getAgentRpcUrl());
        task.setExecutorName(param.getExecutorName());
        task.setContext(param.getContext());
        task.setJobAttributes(param.getAttributes());
        switch (taskType) {
            case STANDALONE:
            case BROADCAST:
            case SHARDING:
            case MAP:
                task.setTaskAttributes((Map<String, Object>) param.getTaskAttributes());
                break;
            case REDUCE:
                List<Map<String, Object>> attr = (List<Map<String, Object>>) param.getTaskAttributes();
                attr = attr.stream()
                        .map(Collections::unmodifiableMap)
                        .collect(Collectors.toList());
                task.setReduceAttributes(attr);
                break;
            default:
                break;
        }
        return task;
    }

}
