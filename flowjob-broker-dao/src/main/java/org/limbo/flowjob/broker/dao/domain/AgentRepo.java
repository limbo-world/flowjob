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
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.agent.AgentRepository;
import org.limbo.flowjob.broker.core.agent.ScheduleAgent;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.AgentEntity;
import org.limbo.flowjob.broker.dao.repositories.AgentEntityRepo;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Collections;
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
    public ScheduleAgent get(String id) {
        AgentEntity agent = agentEntityRepo.findById(id).orElse(null);
        if (agent == null) {
            return null;
        }
        return DomainConverter.toAgent(agent);
    }

    @Override
    public List<ScheduleAgent> findByLastHeartbeatAtBetween(LocalDateTime startTime, LocalDateTime endTime) {
        List<AgentEntity> agentEntities = agentEntityRepo.findByLastHeartbeatAtBetween(startTime, endTime);
        if (CollectionUtils.isEmpty(agentEntities)) {
            return Collections.emptyList();
        }
        return agentEntities.stream().map(DomainConverter::toAgent).collect(Collectors.toList());
    }

}
