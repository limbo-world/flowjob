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
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.console.TaskDTO;
import org.limbo.flowjob.api.param.console.TaskQueryParam;
import org.limbo.flowjob.broker.core.agent.AgentRepository;
import org.limbo.flowjob.broker.core.agent.ScheduleAgent;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Devil
 * @since 2023/4/14
 */
@Slf4j
@Service
public class TaskAppService {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private AgentRepository agentRepository;


    public PageDTO<TaskDTO> page(TaskQueryParam param) {

        JobInstanceEntity jobInstanceEntity = jobInstanceEntityRepo.findById(param.getJobInstanceId()).orElse(null);
        if (jobInstanceEntity == null || StringUtils.isBlank(jobInstanceEntity.getAgentId())) {
            return PageDTO.empty(param);
        }

        ScheduleAgent agent = agentRepository.get(jobInstanceEntity.getAgentId());
        if (agent == null || !agent.isAlive()) {
            return PageDTO.empty(param);
        }

        return agent.page(param);
    }

}
