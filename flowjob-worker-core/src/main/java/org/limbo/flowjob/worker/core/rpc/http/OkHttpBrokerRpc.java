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
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.dto.broker.BrokerTopologyDTO;
import org.limbo.flowjob.api.dto.broker.WorkerRegisterDTO;
import org.limbo.flowjob.api.param.broker.WorkerRegisterParam;
import org.limbo.flowjob.common.lb.BaseLBServer;
import org.limbo.flowjob.common.exception.BrokerRpcException;
import org.limbo.flowjob.common.exception.RegisterFailException;
import org.limbo.flowjob.common.http.OKHttpRpc;
import org.limbo.flowjob.common.lb.LBServerRepository;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.core.rpc.WorkerBrokerRpc;
import org.limbo.flowjob.worker.core.rpc.RpcParamFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.limbo.flowjob.api.constants.rpc.HttpBrokerApi.*;

/**
 * @author Brozen
 * @since 2022-08-31
 */
@Slf4j
public class OkHttpBrokerRpc extends OKHttpRpc<BaseLBServer> implements WorkerBrokerRpc {

    /**
     * Broker 负载均衡
     */
    private final LBServerRepository<BaseLBServer> repository;

    private static final String BASE_URL = "http://0.0.0.0:8080";

    private static final Protocol DEFAULT_PROTOCOL = Protocol.HTTP;

    private String workerId = "";

    public OkHttpBrokerRpc(LBServerRepository<BaseLBServer> repository, LBStrategy<BaseLBServer> strategy) {
        super(repository, strategy);
        this.repository = repository;
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
        ResponseDTO<WorkerRegisterDTO> response = executePost(BASE_URL + API_WORKER_REGISTER, param, new TypeReference<ResponseDTO<WorkerRegisterDTO>>() {
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
        List<BaseLBServer> brokerNodes = repository.listAliveServers().stream().filter(b -> realtime.contains(HttpUrl.get(b.getUrl()))).collect(Collectors.toList());

        // 新增添加的
        Set<HttpUrl> saved = brokerNodes.stream()
                .map(b -> HttpUrl.get(b.getUrl()))
                .collect(Collectors.toSet());
        for (HttpUrl url : realtime) {
            if (saved.contains(url)) {
                continue;
            }

            brokerNodes.add(new BaseLBServer(url.url()));
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
        ResponseDTO<WorkerRegisterDTO> response = executePost(BASE_URL + API_WORKER_HEARTBEAT + "?id=" + workerId, RpcParamFactory.heartbeatParam(worker), new TypeReference<ResponseDTO<WorkerRegisterDTO>>() {
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

}
