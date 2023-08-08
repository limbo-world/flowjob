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

package org.limbo.flowjob.agent.rpc.http;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.agent.Task;
import org.limbo.flowjob.agent.rpc.AgentWorkerRpc;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.param.worker.TaskSubmitParam;
import org.limbo.flowjob.common.exception.RegisterFailException;
import org.limbo.flowjob.common.http.OKHttpRpc;
import org.limbo.flowjob.common.lb.BaseLBServer;
import org.limbo.flowjob.common.meta.Worker;
import org.limbo.flowjob.common.utils.attribute.Attributes;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.limbo.flowjob.api.constants.rpc.HttpWorkerApi.API_SUBMIT_TASK;

/**
 * @author Brozen
 * @since 2022-08-31
 */
@Slf4j
public class OkHttpAgentWorkerRpc extends OKHttpRpc<BaseLBServer> implements AgentWorkerRpc {

    private static final String BASE_URL = "http://0.0.0.0:8080";

    public OkHttpAgentWorkerRpc() {
        super(null, null);
    }

    @Override
    public boolean dispatch(Worker worker, Task task) {
        String baseUrl = BASE_URL;
        if (worker != null) {
            URL url = worker.getUrl();
            baseUrl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
        }
        ResponseDTO<Boolean> response = executePost(baseUrl + API_SUBMIT_TASK, toTaskSubmitParam(task), new TypeReference<ResponseDTO<Boolean>>() {
        });

        if (response == null || !response.success()) {
            String msg = response == null ? MsgConstants.UNKNOWN : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Agent heartbeat failed: " + msg);
        }
        return response.getData();
    }

    public static TaskSubmitParam toTaskSubmitParam(Task task) {
        TaskSubmitParam taskSubmitParam = new TaskSubmitParam();
        taskSubmitParam.setTaskId(task.getTaskId());
        taskSubmitParam.setType(task.getType().type);
        taskSubmitParam.setExecutorName(task.getExecutorName());
        taskSubmitParam.setContext(task.getContext() == null ? Collections.emptyMap() : task.getContext().toMap());
        taskSubmitParam.setAttributes(task.getJobAttributes() == null ? Collections.emptyMap() : task.getJobAttributes().toMap());

        switch (task.getType()) {
            case MAP:
                taskSubmitParam.setMapAttributes(task.getMapAttributes() == null ? Collections.emptyMap() : task.getMapAttributes().toMap());
                break;
            case REDUCE:
                List<Map<String, Object>> reduceAttrs = new LinkedList<>();
                if (CollectionUtils.isNotEmpty(task.getReduceAttributes())) {
                    reduceAttrs = task.getReduceAttributes().stream()
                            .filter(attrs -> attrs != null && !attrs.isEmpty())
                            .map(Attributes::toMap)
                            .collect(Collectors.toList());
                }
                taskSubmitParam.setReduceAttributes(reduceAttrs);
                break;
            default:
                break;
        }
        return taskSubmitParam;
    }

}
