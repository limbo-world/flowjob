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

package org.limbo.flowjob.worker.core.rpc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.limbo.flowjob.broker.api.clent.dto.WorkerRegisterDTO;
import org.limbo.flowjob.broker.api.clent.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerHeartbeatParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.api.dto.BrokerTopologyDTO;
import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.worker.core.rpc.exceptions.BrokerRpcException;
import org.limbo.flowjob.worker.core.rpc.exceptions.RegisterFailException;
import org.limbo.flowjob.worker.core.rpc.lb.LoadBalancer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

    private static final OkHttpClient CLIENT;
    static {
        CLIENT = new OkHttpClient.Builder().build();
    }

    /**
     * Broker 节点信息，包括 OkHttpClient
     */
    private List<BrokerNode> brokerNodes;

    /**
     * Broker 负载均衡
     */
    private LoadBalancer<BrokerNode> loadBalancer;


    public OkHttpBrokerRpc(List<BrokerNode> brokerNodes, LoadBalancer<BrokerNode> loadBalancer) {
        this.brokerNodes = brokerNodes;
        this.loadBalancer = loadBalancer;
    }


    /**
     * {@inheritDoc}
     * @param param 注册参数
     * @return
     */
    @Override
    public WorkerRegisterDTO register(WorkerRegisterParam param) throws RegisterFailException {
        for (BrokerNode broker : brokerNodes) {
            try {
                return registerWith(broker, param);
            } catch (RegisterFailException e) {
                log.error("Register to {} failed，try next node", broker);
            }
        }

        String msg = "Register failed after tried all broker, please check your configuration";
        throw new RegisterFailException(msg);
    }


    /**
     * 向指定 broker 节点发起注册请求
     */
    private WorkerRegisterDTO registerWith(BrokerNode broker, WorkerRegisterParam param) throws RegisterFailException {
        String json = JacksonUtils.toJSONString(param);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);

        Request request = new Request.Builder()
                .url(broker.baseUrl.toString() + "/api/worker/v1")
                .header(HttpHeaders.CONTENT_TYPE, com.google.common.net.MediaType.JSON_UTF_8.toString())
                .post(body).build();
        Call call = CLIENT.newCall(request);
        ResponseDTO<WorkerRegisterDTO> response = execute(call, new TypeReference<ResponseDTO<WorkerRegisterDTO>>() {
        });

        if (response == null || !response.isOk()) {
            String msg = response == null ? "unknown" : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Worker register failed: " + msg);
        }

        // 更新 broker 节点拓扑
        WorkerRegisterDTO result = response.getData();
        updateBrokerTopology(result.getBrokerTopology());

        return result;
    }


    /**
     * 更新 broker 拓扑结构
     */
    private synchronized void updateBrokerTopology(BrokerTopologyDTO topo) {

        Set<URL> saved = brokerNodes.stream()
                .map(b -> b.baseUrl)
                .collect(Collectors.toSet());
        Set<URL> realtime = topo.getBrokers().stream()
                .map(b -> {
                    try {
                        return new URL("http", b.getHost(), b.getPort(), "");
                    } catch (MalformedURLException e) {
                        log.error("Invalid broker URL: {}", b);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 移除配置中过期的
        this.brokerNodes.removeIf(b -> !realtime.contains(b.baseUrl));

        // 新增添加的
        for (URL url : realtime) {
            if (saved.contains(url)) {
                continue;
            }

            this.brokerNodes.add(new BrokerNode(url));
        }

        this.loadBalancer.updateServers(this.brokerNodes);
    }


    /**
     * {@inheritDoc}
     * @param param 心跳参数
     */
    @Override
    public void heartbeat(WorkerHeartbeatParam param) {
        BrokerNode broker = this.loadBalancer.choose();
        String json = JacksonUtils.toJSONString(param);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);

        Request request = new Request.Builder()
                .url(broker.baseUrl.toString() + "/api/worker/v1/heartbeat")
                .header(HttpHeaders.CONTENT_TYPE, com.google.common.net.MediaType.JSON_UTF_8.toString())
                .post(body).build();
        Call call = CLIENT.newCall(request);
        ResponseDTO<BrokerTopologyDTO> response = execute(call, new TypeReference<ResponseDTO<BrokerTopologyDTO>>() {
        });

        if (response == null || !response.isOk()) {
            String msg = response == null ? "unknown" : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Worker heartbeat failed: " + msg);
        }

        // 更新 broker 节点拓扑
        updateBrokerTopology(response.getData());
    }


    /**
     * {@inheritDoc}
     * @param param 执行结果
     */
    @Override
    public void feedbackTask(TaskFeedbackParam param) {
        BrokerNode broker = this.loadBalancer.choose();
        String json = JacksonUtils.toJSONString(param);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);

        Request request = new Request.Builder()
                .url(broker.baseUrl.toString() + "/api/worker/v1/task/feedback")
                .header(HttpHeaders.CONTENT_TYPE, com.google.common.net.MediaType.JSON_UTF_8.toString())
                .post(body).build();
        Call call = CLIENT.newCall(request);
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
