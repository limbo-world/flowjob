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
import okhttp3.HttpUrl;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.agent.core.Job;
import org.limbo.flowjob.agent.core.ScheduleAgent;
import org.limbo.flowjob.agent.core.rpc.AgentBrokerRpc;
import org.limbo.flowjob.agent.core.rpc.RpcParamFactory;
import org.limbo.flowjob.agent.core.worker.Worker;
import org.limbo.flowjob.api.constants.ExecuteResult;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.dto.broker.AgentRegisterDTO;
import org.limbo.flowjob.api.dto.broker.AvailableWorkerDTO;
import org.limbo.flowjob.api.dto.broker.BrokerTopologyDTO;
import org.limbo.flowjob.api.param.broker.AgentRegisterParam;
import org.limbo.flowjob.api.param.broker.JobFeedbackParam;
import org.limbo.flowjob.common.exception.RegisterFailException;
import org.limbo.flowjob.common.http.OKHttpRpc;
import org.limbo.flowjob.common.lb.BaseLBServer;
import org.limbo.flowjob.common.lb.LBServerRepository;
import org.limbo.flowjob.common.lb.LBStrategy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.limbo.flowjob.api.constants.rpc.HttpBrokerApi.*;

/**
 * @author Brozen
 * @since 2022-08-31
 */
@Slf4j
public class OkHttpAgentBrokerRpc extends OKHttpRpc<BaseLBServer> implements AgentBrokerRpc {

    /**
     * Broker 负载均衡
     */
    private final LBServerRepository<BaseLBServer> repository;

    private static final String BASE_URL = "http://0.0.0.0:8080";

    private static final Protocol DEFAULT_PROTOCOL = Protocol.HTTP;

    private String agentId = "";

    public OkHttpAgentBrokerRpc(LBServerRepository<BaseLBServer> repository, LBStrategy<BaseLBServer> strategy) {
        super(repository, strategy);
        this.repository = repository;
    }

    @Override
    public void register(ScheduleAgent agent) throws RegisterFailException {
        AgentRegisterDTO result = null;
        try {
            result = registerWith(RpcParamFactory.registerParam(agent));
        } catch (RegisterFailException e) {
            log.error("Register to broker failed，try next node", e);
        }

        // 注册成功，更新 broker 节点拓扑
        if (result != null) {
            agentId = result.getAgentId();
            updateBrokerTopology(result.getBrokerTopology());
        } else {
            String msg = "Register failed after tried all broker, please check your configuration";
            throw new RegisterFailException(msg);
        }
    }


    /**
     * 向指定 broker 节点发起注册请求
     */
    private AgentRegisterDTO registerWith(AgentRegisterParam param) throws RegisterFailException {
        ResponseDTO<AgentRegisterDTO> response = executePost(BASE_URL + API_AGENT_REGISTER, param, new TypeReference<ResponseDTO<AgentRegisterDTO>>() {
        });

        checkResponse(response, API_AGENT_REGISTER);

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
     * @param agent 发送心跳的对象
     */
    @Override
    public void heartbeat(ScheduleAgent agent) {
        ResponseDTO<AgentRegisterDTO> response = executePost(BASE_URL + API_AGENT_HEARTBEAT + "?id=" + agentId, RpcParamFactory.heartbeatParam(agent), new TypeReference<ResponseDTO<AgentRegisterDTO>>() {
        });

        checkResponse(response, API_AGENT_HEARTBEAT);

        // 更新 broker 节点拓扑
        if (response.getData() != null) {
            AgentRegisterDTO data = response.getData();
            updateBrokerTopology(data.getBrokerTopology());
        }
    }

    @Override
    public boolean notifyJobExecuting(String jobInstanceId) {
        String url = BASE_URL + API_JOB_EXECUTING + "?agentId=" + agentId + "&jobInstanceId=" + jobInstanceId;
        ResponseDTO<Boolean> response = executePost(url, null, new TypeReference<ResponseDTO<Boolean>>() {
        });

        checkResponse(response, API_JOB_EXECUTING);

        return response.getData();
    }

    @Override
    public boolean feedbackJobSucceed(Job job) {
        JobFeedbackParam param = new JobFeedbackParam();
        param.setContext(job.getContext().toMap());
        param.setResult(ExecuteResult.SUCCEED);
        String url = BASE_URL + API_JOB_FEEDBACK + "?jobInstanceId=" + job.getId();
        ResponseDTO<Boolean> response = executePost(url, param, new TypeReference<ResponseDTO<Boolean>>() {
        });

        checkResponse(response, API_JOB_FEEDBACK);

        return response.getData();
    }

    @Override
    public boolean feedbackJobFail(Job job, String errorMsg) {
        JobFeedbackParam param = new JobFeedbackParam();
        param.setErrorMsg(errorMsg);
        param.setResult(ExecuteResult.FAILED);
        String url = BASE_URL + API_JOB_FEEDBACK + "?jobInstanceId=" + job.getId();
        ResponseDTO<Boolean> response = executePost(url, param, new TypeReference<ResponseDTO<Boolean>>() {
        });

        checkResponse(response, API_JOB_FEEDBACK);

        return response.getData();
    }

    @Override
    public List<Worker> availableWorkers(String jobId, boolean filterExecutor, boolean filterTag, boolean filterResource) {
        String url = String.format(BASE_URL + API_JOB_FILTER_WORKERS + "?jobInstanceId=%s&filterExecutor=%s&filterTag=%s&filterResource=%s", jobId, filterExecutor, filterTag, filterResource);
        ResponseDTO<List<AvailableWorkerDTO>> response = executeGet(url, new TypeReference<ResponseDTO<List<AvailableWorkerDTO>>>() {
        });

        checkResponse(response, API_JOB_FILTER_WORKERS);
        List<AvailableWorkerDTO> workerDTOS = response.getData();
        if (CollectionUtils.isEmpty(workerDTOS)) {
            return Collections.emptyList();
        }
        List<Worker> workers = new ArrayList<>();
        for (AvailableWorkerDTO workerDTO : workerDTOS) {
            try {
                workers.add(toWorker(workerDTO));
            } catch (Exception e) {
                log.error("convert dto to worker file dto={}", workerDTO, e);
            }
        }
        return workers;
    }

    public Worker toWorker(AvailableWorkerDTO workerDTO) throws MalformedURLException {
        URL url = new URL(workerDTO.getProtocol(), workerDTO.getHost(), workerDTO.getPort(), "");
        return new Worker(workerDTO.getId(), url);
    }


    public void checkResponse(ResponseDTO<?> response, String path) {
        if (response == null || !response.success()) {
            String msg = response == null ? MsgConstants.UNKNOWN : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Agent " + path + " failed: " + msg);
        }
    }

}
