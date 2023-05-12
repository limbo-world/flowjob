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

package org.limbo.flowjob.worker.core.rpc.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.ResponseDTO;
import org.limbo.flowjob.api.remote.dto.BrokerTopologyDTO;
import org.limbo.flowjob.api.remote.dto.WorkerRegisterDTO;
import org.limbo.flowjob.api.remote.param.TaskFeedbackParam;
import org.limbo.flowjob.api.remote.param.WorkerRegisterParam;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.common.lb.LBServerRepository;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.core.executor.ExecuteContext;
import org.limbo.flowjob.worker.core.rpc.BrokerNode;
import org.limbo.flowjob.worker.core.rpc.BrokerRpc;
import org.limbo.flowjob.worker.core.rpc.RpcParamFactory;
import org.limbo.flowjob.worker.core.rpc.exceptions.BrokerRpcException;
import org.limbo.flowjob.worker.core.rpc.exceptions.RegisterFailException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-08-31
 */
@Slf4j
public class OkHttpBrokerRpc implements BrokerRpc {

    private final OkHttpClient client;

    /**
     * Broker 负载均衡
     */
    private final LBServerRepository<BrokerNode> repository;

    // application/json; charset=utf-8
    private static final String JSON_UTF_8 = com.google.common.net.MediaType.JSON_UTF_8.toString();

    private static final MediaType MEDIA_TYPE = MediaType.parse(JSON_UTF_8);

    private static final String BASE_URL = "http://0.0.0.0:8080";

    private static final Protocol DEFAULT_PROTOCOL = Protocol.HTTP;

    private String workerId = "";

    public OkHttpBrokerRpc(LBServerRepository<BrokerNode> repository, LBStrategy<BrokerNode> strategy) {
        this.repository = repository;
        this.client = new OkHttpClient.Builder().addInterceptor(new LoadBalanceInterceptor<>(repository, strategy)).build();
    }

    /**
     * {@inheritDoc}
     *
     * @param worker 需注册的 Worker
     * @return
     * @throws RegisterFailException
     */
    @Override
    public void register(Worker worker) throws RegisterFailException {
        WorkerRegisterDTO result = null;
        try {
            result = registerWith(RpcParamFactory.registerParam(worker));
        } catch (RegisterFailException e) {
            log.error("Register to broker failed，try next node", e);
        }

        // 注册成功，更新 broker 节点拓扑
        if (result != null) {
            workerId = result.getWorkerId();
            updateBrokerTopology(result.getBrokerTopology());
        } else {
            String msg = "Register failed after tried all broker, please check your configuration";
            throw new RegisterFailException(msg);
        }
    }


    /**
     * 向指定 broker 节点发起注册请求
     */
    private WorkerRegisterDTO registerWith(WorkerRegisterParam param) throws RegisterFailException {
        ResponseDTO<WorkerRegisterDTO> response = executePost(BASE_URL + "/api/v1/rpc/worker", param, new TypeReference<ResponseDTO<WorkerRegisterDTO>>() {
        });

        if (response == null || !response.success()) {
            String msg = response == null ? MsgConstants.UNKNOWN : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Worker register failed: " + msg);
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
     * {@inheritDoc}
     *
     * @param worker 发送心跳的 Worker
     */
    @Override
    public void heartbeat(Worker worker) {
        ResponseDTO<WorkerRegisterDTO> response = executePost(BASE_URL + "/api/v1/rpc/worker/" + workerId + "/heartbeat", RpcParamFactory.heartbeatParam(worker), new TypeReference<ResponseDTO<WorkerRegisterDTO>>() {
        });

        if (response == null || !response.success()) {
            String msg = response == null ? MsgConstants.UNKNOWN : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Worker heartbeat failed: " + msg);
        }

        // 更新 broker 节点拓扑
        if (response.getData() != null) {
            WorkerRegisterDTO data = response.getData();
            updateBrokerTopology(data.getBrokerTopology());
        }
    }


    /**
     * {@inheritDoc}
     *
     * @param context 任务执行上下文
     */
    @Override
    public void feedbackTaskSucceed(ExecuteContext context) {
        Task task = context.getTask();
        doFeedbackTask(task.getTaskId(), RpcParamFactory.taskFeedbackParam(task.getContext(), task.getJobAttributes(), task.getResult(), null));
    }


    /**
     * {@inheritDoc}
     *
     * @param context 任务执行上下文
     * @param ex      导致任务失败的异常信息
     */
    @Override
    public void feedbackTaskFailed(ExecuteContext context, @Nullable Throwable ex) {
        Task task = context.getTask();
        doFeedbackTask(context.getTask().getTaskId(), RpcParamFactory.taskFeedbackParam(task.getContext(), task.getJobAttributes(), task.getResult(), ex));
    }


    /**
     * 反馈任务执行结果
     */
    private void doFeedbackTask(String taskId, TaskFeedbackParam feedbackParam) {
        ResponseDTO<Void> response = executePost(BASE_URL + "/api/v1/rpc/worker/task/" + taskId + "/feedback", feedbackParam, new TypeReference<ResponseDTO<Void>>() {
        });

        if (response == null || !response.success()) {
            String msg = response == null ? MsgConstants.UNKNOWN : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Worker feedback Task failed: " + msg);
        }
    }

    /**
     * 通过 OkHttp 执行请求，并获取响应
     */
    private <T> ResponseDTO<T> executePost(String url, Object param, TypeReference<ResponseDTO<T>> reference) {
        Objects.requireNonNull(reference);

        String json = JacksonUtils.toJSONString(param);
        RequestBody body = RequestBody.create(MEDIA_TYPE, json);

        Request request = new Request.Builder()
                .url(url)
                .header(HttpHeaders.CONTENT_TYPE, JSON_UTF_8)
                .post(body).build();
        Call call = client.newCall(request);

        if (log.isDebugEnabled()) {
            log.debug("call broker {}", logRequest(url, json));
        }

        try {
            // HTTP 响应状态异常
            Response response = call.execute();
            if (!response.isSuccessful()) {
                throw new BrokerRpcException("Broker api access failed; " + logRequest(url, json) + " code=" + response.code());
            }

            // 无响应 body 是异常
            if (response.body() == null) {
                throw new BrokerRpcException("Broker api response empty body " + logRequest(url, json));
            }
            return JacksonUtils.parseObject(response.body().string(), reference);
        } catch (IOException e) {
            throw new BrokerRpcException("Broker api access failed " + logRequest(url, json), e);
        }
    }

    private String logRequest(String url, String param) {
        return String.format("request[url=%s, param=%s]", url, param);
    }

}
