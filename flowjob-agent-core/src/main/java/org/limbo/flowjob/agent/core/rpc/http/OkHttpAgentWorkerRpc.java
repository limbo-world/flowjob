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

package org.limbo.flowjob.agent.core.rpc.http;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.core.entity.Task;
import org.limbo.flowjob.agent.core.Worker;
import org.limbo.flowjob.agent.core.rpc.AgentWorkerRpc;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.param.worker.TaskSubmitParam;
import org.limbo.flowjob.common.exception.RegisterFailException;
import org.limbo.flowjob.common.exception.RpcException;
import org.limbo.flowjob.common.http.OKHttpRpc;
import org.limbo.flowjob.common.lb.BaseLBServer;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.limbo.flowjob.api.constants.rpc.HttpWorkerApi.API_SUBMIT_TASK;

/**
 * @author Brozen
 * @since 2022-08-31
 */
@Slf4j
public class OkHttpAgentWorkerRpc extends OKHttpRpc<BaseLBServer> implements AgentWorkerRpc {

    private URL agentUrl;

    public OkHttpAgentWorkerRpc(URL agentUrl) {
        super(null, null);
        this.agentUrl = agentUrl;
    }

    @Override
    public boolean dispatch(Worker worker, Task task) {
        if (worker == null) {
            return false;
        }
        URL url = worker.getUrl();
        String baseUrl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
        ResponseDTO<Boolean> response = executePost(baseUrl + API_SUBMIT_TASK, toTaskSubmitParam(agentUrl, task), new TypeReference<ResponseDTO<Boolean>>() {
        });

        if (response == null || !response.success()) {
            String msg = response == null ? MsgConstants.UNKNOWN : (response.getCode() + ":" + response.getMessage());
            throw new RpcException("Agent heartbeat failed: " + msg);
        }
        return response.getData();
    }

    public static TaskSubmitParam toTaskSubmitParam(URL agentUrl, Task task) {
        TaskSubmitParam taskSubmitParam = new TaskSubmitParam();
        taskSubmitParam.setJobId(task.getJobId());
        taskSubmitParam.setTaskId(task.getTaskId());
        taskSubmitParam.setAgentRpcUrl(agentUrl);
        taskSubmitParam.setType(task.getType().type);
        taskSubmitParam.setExecutorName(task.getExecutorName());
        taskSubmitParam.setContext(task.getContext() == null ? Collections.emptyMap() : task.getContext().toMap());
        taskSubmitParam.setAttributes(task.getJobAttributes() == null ? Collections.emptyMap() : task.getJobAttributes().toMap());

        switch (task.getType()) {
            case MAP:
                if (StringUtils.isBlank(task.getTaskAttributes())) {
                    taskSubmitParam.setTaskAttributes(Collections.emptyMap());
                } else {
                    taskSubmitParam.setTaskAttributes(JacksonUtils.parseObject(task.getTaskAttributes(), new TypeReference<Map<String, Object>>() {
                    }));
                }
                break;
            case REDUCE:
                if (StringUtils.isBlank(task.getTaskAttributes())) {
                    taskSubmitParam.setTaskAttributes(Collections.emptyList());
                } else {
                    List<Map<String, Object>> reduceAttrs = JacksonUtils.parseObject(task.getTaskAttributes(), new TypeReference<List<Map<String, Object>>>() {
                    });
                    taskSubmitParam.setTaskAttributes(reduceAttrs);
                }
                break;
            default:
                break;
        }
        return taskSubmitParam;
    }

}
