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
import org.limbo.flowjob.broker.api.clent.dto.WorkerRegisterDTO;
import org.limbo.flowjob.broker.api.clent.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.api.dto.BrokerTopologyDTO;
import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.core.executor.ExecuteContext;
import org.limbo.flowjob.worker.core.rpc.BrokerNode;
import org.limbo.flowjob.worker.core.rpc.BrokerRpc;
import org.limbo.flowjob.worker.core.rpc.LoadBalancer;
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
    private final LoadBalancer<BrokerNode> loadBalancer;

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json;charset=utf-8");

    private static final String BASE_URL = "http://0.0.0.0:8080";

    public OkHttpBrokerRpc(LoadBalancer<BrokerNode> loadBalancer) {
        this.client = new OkHttpClient.Builder().addInterceptor(new LoadBalanceInterceptor(loadBalancer, 10)).build();
        this.loadBalancer = loadBalancer;
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
            // FIXME 临时注释掉拓扑更新，broker 还没返回拓扑结构
            // updateBrokerTopology(result.getBrokerTopology());
        } else {
            String msg = "Register failed after tried all broker, please check your configuration";
            throw new RegisterFailException(msg);
        }
    }


    /**
     * 向指定 broker 节点发起注册请求
     */
    private WorkerRegisterDTO registerWith(WorkerRegisterParam param) throws RegisterFailException {
        String json = JacksonUtils.toJSONString(param);
        RequestBody body = RequestBody.create(MEDIA_TYPE, json);

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/worker/v1/worker")
                .header(HttpHeaders.CONTENT_TYPE, com.google.common.net.MediaType.JSON_UTF_8.toString())
                .post(body).build();
        Call call = client.newCall(request);
        ResponseDTO<WorkerRegisterDTO> response = execute(call, new TypeReference<ResponseDTO<WorkerRegisterDTO>>() {
        });

        if (response == null || !response.isOk()) {
            String msg = response == null ? "unknown" : (response.getCode() + ":" + response.getMessage());
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
                .map(b -> new HttpUrl.Builder().scheme(b.getProtocol()).host(b.getHost()).port(b.getPort()).build())
                .collect(Collectors.toSet());
        List<BrokerNode> brokerNodes = loadBalancer.listAliveServers().stream().filter(b -> realtime.contains(HttpUrl.get(b.baseUrl))).collect(Collectors.toList());

        // 新增添加的
        Set<HttpUrl> saved = brokerNodes.stream()
                .map(b -> HttpUrl.get(b.baseUrl))
                .collect(Collectors.toSet());
        for (HttpUrl url : realtime) {
            if (saved.contains(url)) {
                continue;
            }

            brokerNodes.add(new BrokerNode(url.url()));
        }

        this.loadBalancer.updateServers(brokerNodes);
    }

    /**
     * {@inheritDoc}
     *
     * @param worker 发送心跳的 Worker
     */
    @Override
    public void heartbeat(Worker worker) {
        String json = JacksonUtils.toJSONString(RpcParamFactory.heartbeatParam(worker));
        RequestBody body = RequestBody.create(MEDIA_TYPE, json);

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/v1/worker/heartbeat")
                .header(HttpHeaders.CONTENT_TYPE, com.google.common.net.MediaType.JSON_UTF_8.toString())
                .post(body).build();
        Call call = client.newCall(request);
        ResponseDTO<BrokerTopologyDTO> response = execute(call, new TypeReference<ResponseDTO<BrokerTopologyDTO>>() {
        });

        if (response == null || !response.isOk()) {
            String msg = response == null ? "unknown" : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Worker heartbeat failed: " + msg);
        }

        // 更新 broker 节点拓扑
        if (response.getData() != null) {
            updateBrokerTopology(response.getData());
        }
    }


    /**
     * {@inheritDoc}
     *
     * @param context 任务执行上下文
     */
    @Override
    public void feedbackTaskSucceed(ExecuteContext context) {
        doFeedbackTask(RpcParamFactory.taskFeedbackParam(context.getTask().getTaskId(), null));
    }


    /**
     * {@inheritDoc}
     *
     * @param context 任务执行上下文
     * @param ex      导致任务失败的异常信息
     */
    @Override
    public void feedbackTaskFailed(ExecuteContext context, @Nullable Throwable ex) {
        doFeedbackTask(RpcParamFactory.taskFeedbackParam(context.getTask().getTaskId(), ex));
    }


    /**
     * 反馈任务执行结果
     */
    private void doFeedbackTask(TaskFeedbackParam feedbackParam) {
        String json = JacksonUtils.toJSONString(feedbackParam);
        RequestBody body = RequestBody.create(MEDIA_TYPE, json);

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/worker/v1/task/feedback")
                .header(HttpHeaders.CONTENT_TYPE, com.google.common.net.MediaType.JSON_UTF_8.toString())
                .post(body).build();
        Call call = client.newCall(request);
        ResponseDTO<Void> response = execute(call);

        if (response == null || !response.isOk()) {
            String msg = response == null ? "unknown" : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Worker register failed: " + msg);
        }
    }


    /**
     * 通过 OkHttp 执行请求，并获取响应
     */
    private ResponseDTO<Void> execute(Call call) {
        return execute(call, new TypeReference<ResponseDTO<Void>>() {
        });
    }


    /**
     * 通过 OkHttp 执行请求，并获取响应
     */
    private <T> ResponseDTO<T> execute(Call call, TypeReference<ResponseDTO<T>> reference) {
        Objects.requireNonNull(reference);

        try {
            // HTTP 响应状态异常
            Response response = call.execute();
            if (!response.isSuccessful()) {
                throw new BrokerRpcException("Broker api access failed: code=" + response.code());
            }

            // 无响应 body 是异常
            if (response.body() == null) {
                throw new BrokerRpcException("Broker api response empty body");
            }
            return JacksonUtils.parseObject(response.body().string(), reference);
        } catch (IOException e) {
            throw new BrokerRpcException("Broker api access failed", e);
        }
    }

}
