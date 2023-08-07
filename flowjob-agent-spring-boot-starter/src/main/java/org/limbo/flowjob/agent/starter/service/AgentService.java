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

package org.limbo.flowjob.agent.starter.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.agent.Job;
import org.limbo.flowjob.agent.ScheduleAgent;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.param.agent.JobSubmitParam;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.springframework.stereotype.Service;

/**
 * @author Devil
 * @since 2023/8/7
 */
@Slf4j
@Service("fjaAgentService")
@AllArgsConstructor
public class AgentService {

    private ScheduleAgent agent;

    /**
     * {@inheritDoc}
     * @param param
     * @return
     */
    public Boolean receive(JobSubmitParam param) {
        log.info("receive task {}", param);
        try {
            Job job = convert(param);
            agent.receiveJob(job);
            return true;
        } catch (Exception e) {
            log.error("Failed to receive task", e);
            return false;
        }
    }

    public Job convert(JobSubmitParam param) {
        Job job = new Job();
        job.setId(param.getId());
        job.setType(JobType.parse(param.getType()));
        job.setExecutorName(param.getExecutorName());
        job.setContext(new Attributes(param.getContext()));
        job.setAttributes(new Attributes(param.getAttributes()));
        return job;
    }

}
