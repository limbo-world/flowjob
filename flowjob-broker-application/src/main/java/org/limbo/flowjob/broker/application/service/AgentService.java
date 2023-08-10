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

package org.limbo.flowjob.broker.application.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.constants.AgentStatus;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.api.dto.broker.AgentRegisterDTO;
import org.limbo.flowjob.api.param.broker.AgentHeartbeatParam;
import org.limbo.flowjob.api.param.broker.AgentRegisterParam;
import org.limbo.flowjob.broker.application.converter.BrokerConverter;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.broker.dao.entity.AgentEntity;
import org.limbo.flowjob.broker.dao.repositories.AgentEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.net.URL;
import java.util.Optional;

/**
 * @author Devil
 * @since 2023/8/6
 */
@Slf4j
@Service
public class AgentService {

    @Setter(onMethod_ = @Inject)
    private AgentEntityRepo agentEntityRepo;

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    @Setter(onMethod_ = @Inject)
    private NodeManger nodeManger;

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    /**
     * 注册
     *
     * @param options 注册参数
     * @return 返回所有broker节点信息
     */
    @Transactional(rollbackOn = Throwable.class)
    public AgentRegisterDTO register(AgentRegisterParam options) {
        // 校验 protocol
        String protocolName = options.getUrl().getProtocol();
        Protocol protocol = Protocol.parse(protocolName);
        Verifies.verify(
                protocol != Protocol.UNKNOWN,
                MsgConstants.UNKNOWN + " agent rpc protocol:" + protocolName
        );

        URL url = options.getUrl();

        // 新增 or 更新
        AgentEntity agent = Optional
                .ofNullable(agentEntityRepo.findByHostAndPort(url.getHost(), url.getPort()))
                .orElseGet(() -> newAgent(options));
        // 保存
        agentEntityRepo.saveAndFlush(agent);

        log.info("worker registered " + agent);

        return toDTO(agent.getAgentId());
    }

    /**
     * 心跳
     *
     * @param option 心跳参数，上报部分指标数据
     */
    @Transactional(rollbackOn = Throwable.class)
    public AgentRegisterDTO heartbeat(String agentId, AgentHeartbeatParam option) {
        AgentEntity agent = agentEntityRepo.findById(agentId).get();
        Verifies.requireNotNull(agent, "agent不存在！");

        // 更新
        agentEntityRepo.updateQueue(agentId, option.getAvailableResource().getAvailableQueueLimit());

        if (log.isDebugEnabled()) {
            log.debug("receive heartbeat from " + agentId);
        }

        return toDTO(agentId);
    }


    @Transactional(rollbackOn = Throwable.class)
    public boolean jobDispatched(String agentId, String jobInstanceId) {
        return jobInstanceEntityRepo.executing(agentId, jobInstanceId, TimeUtils.currentLocalDateTime()) > 0;
    }


    public AgentRegisterDTO toDTO(String agentId) {
        AgentRegisterDTO registerResult = new AgentRegisterDTO();
        registerResult.setAgentId(agentId);
        registerResult.setBrokerTopology(BrokerConverter.toBrokerTopologyDTO(nodeManger.allAlive()));
        return registerResult;
    }

    public AgentEntity newAgent(AgentRegisterParam options) {
        AgentEntity agent = new AgentEntity();
        agent.setAgentId(idGenerator.generateId(IDType.AGENT));
        agent.setProtocol(options.getUrl().getProtocol());
        agent.setHost(options.getUrl().getHost());
        agent.setPort(options.getUrl().getPort());
        agent.setStatus(AgentStatus.TERMINATED.status);
//        agent.setAvailableQueueLimit(options.getAvailableResource().getAvailableQueueLimit());
        return agent;
    }
}
