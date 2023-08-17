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

package org.limbo.flowjob.broker.dao.domain;

import lombok.Setter;
import org.limbo.flowjob.api.constants.AgentStatus;
import org.limbo.flowjob.broker.core.agent.AgentRepository;
import org.limbo.flowjob.broker.core.agent.ScheduleAgent;
import org.limbo.flowjob.broker.dao.entity.AgentEntity;
import org.limbo.flowjob.broker.dao.repositories.AgentEntityRepo;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/8/14
 */
@Repository
public class AgentRepo implements AgentRepository {

    @Setter(onMethod_ = @Inject)
    protected AgentEntityRepo agentEntityRepo;

    @Override
    public List<ScheduleAgent> listAvailableAgents() {
        List<AgentEntity> agentEntities = agentEntityRepo.findByStatusAndAvailableQueueLimitGreaterThanAndEnabledAndDeleted(AgentStatus.RUNNING.status, 0, true, false);
        return agentEntities.stream().map(this::toAgent).collect(Collectors.toList());
    }

    @Override
    public ScheduleAgent get(String id) {
        AgentEntity agent = agentEntityRepo.findById(id).orElse(null);
        if (agent == null) {
            return null;
        }
        return toAgent(agent);
    }


    public ScheduleAgent toAgent(AgentEntity entity) {
        return ScheduleAgent.builder()
                .id(entity.getAgentId())
                .status(AgentStatus.parse(entity.getStatus()))
                .rpcBaseUrl(url(entity))
                .build();

    }

    public static URL url(AgentEntity entity) {
        try {
            return new URL(entity.getProtocol(), entity.getHost(), entity.getPort(), "");
        } catch (Exception e) {
            throw new IllegalStateException("parse agent rpc info error", e);
        }
    }

}
