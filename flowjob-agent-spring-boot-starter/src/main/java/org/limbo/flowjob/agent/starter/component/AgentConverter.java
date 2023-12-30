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

package org.limbo.flowjob.agent.starter.component;

import lombok.Setter;
import org.limbo.flowjob.agent.core.TaskDispatcher;
import org.limbo.flowjob.agent.core.entity.Job;
import org.limbo.flowjob.agent.core.repository.JobRepository;
import org.limbo.flowjob.agent.core.repository.TaskRepository;
import org.limbo.flowjob.agent.core.rpc.AgentBrokerRpc;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.constants.LoadBalanceType;
import org.limbo.flowjob.api.param.agent.JobSubmitParam;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2023/12/27
 */
@Component
public class AgentConverter {

    @Setter
    private TaskDispatcher taskDispatcher;

    @Setter
    private TaskRepository taskRepository;

    @Setter
    private JobRepository jobRepository;

    @Setter
    private AgentBrokerRpc brokerRpc;

    public Job convert(JobSubmitParam param) {
        return Job.builder()
                .id(param.getJobInstanceId())
                .instanceId(param.getPlanInstanceId())
                .type(JobType.parse(param.getType()))
                .executorName(param.getExecutorName())
                .loadBalanceType(LoadBalanceType.parse(param.getLoadBalanceType()))
                .context(new Attributes(param.getContext()))
                .attributes(new Attributes(param.getAttributes()))
                .taskDispatcher(taskDispatcher)
                .taskRepository(taskRepository)
                .jobRepository(jobRepository)
                .brokerRpc(brokerRpc)
                .build();
    }
}
