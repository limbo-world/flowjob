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
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.agent.Task;
import org.limbo.flowjob.agent.rpc.AgentWorkerRpc;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.dto.broker.AgentRegisterDTO;
import org.limbo.flowjob.api.dto.broker.BrokerTopologyDTO;
import org.limbo.flowjob.api.param.broker.AgentRegisterParam;
import org.limbo.flowjob.api.param.worker.TaskSubmitParam;
import org.limbo.flowjob.common.cluster.BrokerNode;
import org.limbo.flowjob.common.cluster.WorkerNode;
import org.limbo.flowjob.common.exception.BrokerRpcException;
import org.limbo.flowjob.common.exception.RegisterFailException;
import org.limbo.flowjob.common.http.OKHttpRpc;
import org.limbo.flowjob.common.lb.LBServerRepository;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.limbo.flowjob.api.constants.rpc.HttpBrokerApi.API_AGENT_REGISTER;
import static org.limbo.flowjob.api.constants.rpc.HttpWorkerApi.API_SUBMIT_TASK;

/**
 * @author Brozen
 * @since 2022-08-31
 */
@Slf4j
public class OkHttpAgentWorkerRpc extends OKHttpRpc<WorkerNode> implements AgentWorkerRpc {

    /**
     * Broker 负载均衡
     */
    private final LBServerRepository<WorkerNode> repository;

    private static final String BASE_URL = "http://0.0.0.0:8080";

    private static final Protocol DEFAULT_PROTOCOL = Protocol.HTTP;

    private String agentId = "";

    public OkHttpAgentWorkerRpc(LBServerRepository<WorkerNode> repository, LBStrategy<WorkerNode> strategy) {
        super(repository, strategy);
        this.repository = repository;
    }

    @Override
    public boolean dispatch(Task task) {
        ResponseDTO<Boolean> response = executePost(BASE_URL + API_SUBMIT_TASK, toTaskSubmitParam(task), new TypeReference<ResponseDTO<Boolean>>() {
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
        taskSubmitParam.setJobInstanceId(task.getJobInstanceId());
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

    /**
     * 向指定 broker 节点发起注册请求
     */
    private AgentRegisterDTO registerWith(AgentRegisterParam param) throws RegisterFailException {
        ResponseDTO<AgentRegisterDTO> response = executePost(BASE_URL + API_AGENT_REGISTER, param, new TypeReference<ResponseDTO<AgentRegisterDTO>>() {
        });

        if (response == null || !response.success()) {
            String msg = response == null ? MsgConstants.UNKNOWN : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Agent register failed: " + msg);
        }

        return response.getData();
    }

    /**
     * 更新 broker 拓扑结构
     */
    private synchronized void updateBrokerTopology(BrokerTopologyDTO topo) {
        if (topo == null || CollectionUtils.isEmpty(topo.getBrokers())) {
            throw new IllegalStateException("Broker topology error: " + topo);
        }

        // 移除接口返回中不存在的，这批节点已经下线
        Set<HttpUrl> realtime = topo.getBrokers().stream()
                .map(b -> new HttpUrl.Builder().scheme(DEFAULT_PROTOCOL.value).host(b.getHost()).port(b.getPort()).build())
                .collect(Collectors.toSet());
        List<BrokerNode> brokerNodes = repository.listAliveServers().stream().filter(b -> realtime.contains(HttpUrl.get(b.getUrl()))).collect(Collectors.toList());

        // 新增添加的
        Set<HttpUrl> saved = brokerNodes.stream()
                .map(b -> HttpUrl.get(b.getUrl()))
                .collect(Collectors.toSet());
        for (HttpUrl url : realtime) {
            if (saved.contains(url)) {
                continue;
            }

            brokerNodes.add(new BrokerNode(url.url()));
        }

        repository.updateServers(brokerNodes);
    }

    /**
     * 通过 OkHttp 执行请求，并获取响应
     */
    private <T> ResponseDTO<T> executePost(String url, Object param, TypeReference<ResponseDTO<T>> reference) {
        Objects.requireNonNull(reference);

        ResponseBody responseBody = executePost(url, param);
        try {
            return JacksonUtils.parseObject(responseBody.string(), reference);
        } catch (IOException e) {
            throw new BrokerRpcException("Api access failed " + logRequest(url, JacksonUtils.toJSONString(param)), e);
        }
    }

    /**
     * 通过 OkHttp 执行请求，并获取响应
     */
    private <T> ResponseDTO<T> executeGet(String url, TypeReference<ResponseDTO<T>> reference) {
        Objects.requireNonNull(reference);

        ResponseBody responseBody = executeGet(url);
        try {
            return JacksonUtils.parseObject(responseBody.string(), reference);
        } catch (IOException e) {
            throw new BrokerRpcException("Api access failed " + logRequest(url), e);
        }
    }
}
