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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.limbo.flowjob.broker.api.clent.dto.WorkerRegisterDTO;
import org.limbo.flowjob.broker.api.clent.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerExecutorRegisterParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerHeartbeatParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerResourceParam;
import org.limbo.flowjob.broker.api.constants.enums.ExecuteResult;
import org.limbo.flowjob.broker.api.dto.BrokerTopologyDTO;
import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.core.domain.WorkerResources;
import org.limbo.flowjob.worker.core.executor.ExecuteContext;
import org.limbo.flowjob.worker.core.rpc.exceptions.BrokerRpcException;
import org.limbo.flowjob.worker.core.rpc.exceptions.RegisterFailException;
import org.limbo.flowjob.common.lb.LoadBalancer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
     * @param worker 需注册的 Worker
     * @return
     * @throws RegisterFailException
     */
    @Override
    public void register(Worker worker) throws RegisterFailException {
        WorkerRegisterDTO result = null;
        for (BrokerNode broker : brokerNodes) {
            try {
                result = registerWith(broker, registerParam(worker));
                break;
            } catch (RegisterFailException e) {
                log.error("Register to {} failed，try next node", broker);
            }
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
     * 封装 Worker 注册参数
     */
    private WorkerRegisterParam registerParam(Worker worker) {
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
        registerParam.setId(worker.getId());
        registerParam.setUrl(workerRpcBaseURL);
        registerParam.setExecutors(executors);
        registerParam.setAvailableResource(resourceParam);
        registerParam.setTags(tags);

        return registerParam;
    }


    /**
     * 向指定 broker 节点发起注册请求
     */
    private WorkerRegisterDTO registerWith(BrokerNode broker, WorkerRegisterParam param) throws RegisterFailException {
        String json = JacksonUtils.toJSONString(param);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);

        Request request = new Request.Builder()
                .url(broker.baseUrl.toString() + "/api/worker/v1/worker")
                .header(HttpHeaders.CONTENT_TYPE, com.google.common.net.MediaType.JSON_UTF_8.toString())
                .post(body).build();
        Call call = CLIENT.newCall(request);
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

        Set<URL> saved = brokerNodes.stream()
                .map(b -> b.baseUrl)
                .collect(Collectors.toSet());
        Set<URL> realtime = topo.getBrokers().stream()
                .map(b -> {
                    try {
                        // FIXME 先写死 http 协议通信，后面需协商得出
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
     * @param worker 发送心跳的 Worker
     */
    @Override
    public void heartbeat(Worker worker) {
        BrokerNode broker = choose();
        String json = JacksonUtils.toJSONString(heartbeatParam(worker));
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);

        Request request = new Request.Builder()
                .url(broker.baseUrl.toString() + "/api/v1/worker/heartbeat")
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
        if (response.getData() != null) {
            updateBrokerTopology(response.getData());
        }
    }


    /**
     * 封装 Worker 心跳参数
     */
    private WorkerHeartbeatParam heartbeatParam(Worker worker) {
        // 可用资源
        WorkerResources workerResource = worker.getResource();
        WorkerResourceParam resource = new WorkerResourceParam();
        resource.setAvailableCpu(workerResource.availableCpu());
        resource.setAvailableRAM(workerResource.availableRam());
        resource.setAvailableQueueLimit(workerResource.availableQueueSize());

        // 组装心跳参数
        WorkerHeartbeatParam heartbeatParam = new WorkerHeartbeatParam();
        heartbeatParam.setWorkerId(worker.getId());
        heartbeatParam.setAvailableResource(resource);

        return heartbeatParam;
    }


    /**
     * {@inheritDoc}
     * @param context 任务执行上下文
     */
    @Override
    public void feedbackTaskSucceed(ExecuteContext context) {
        TaskFeedbackParam feedbackParam = new TaskFeedbackParam();
        feedbackParam.setTaskId(context.getTask().getTaskId());
        feedbackParam.setResult((int) ExecuteResult.SUCCEED.result);

        doFeedbackTask(feedbackParam);
    }


    /**
     * {@inheritDoc}
     * @param context 任务执行上下文
     * @param ex 导致任务失败的异常信息，可以为 null
     */
    @Override
    public void feedbackTaskFailed(ExecuteContext context, @Nullable Throwable ex) {
        TaskFeedbackParam feedbackParam = new TaskFeedbackParam();
        feedbackParam.setTaskId(context.getTask().getTaskId());
        feedbackParam.setResult((int) ExecuteResult.FAILED.result);

        if (ex != null) {
            feedbackParam.setErrorStackTrace(ExceptionUtils.getStackTrace(ex));
        }

        doFeedbackTask(feedbackParam);
    }


    /**
     * 反馈任务执行结果
     */
    private void doFeedbackTask(TaskFeedbackParam feedbackParam) {
        BrokerNode broker = choose();
        String json = JacksonUtils.toJSONString(feedbackParam);
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
     * {@inheritDoc}
     * @return
     */
    private BrokerNode choose() {
        while (CollectionUtils.isNotEmpty(this.loadBalancer.listAliveServers())) {
            Optional<BrokerNode> optional = this.loadBalancer.choose();
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        throw new IllegalStateException("Can't get alive broker，LB=" + this.loadBalancer.name());
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
