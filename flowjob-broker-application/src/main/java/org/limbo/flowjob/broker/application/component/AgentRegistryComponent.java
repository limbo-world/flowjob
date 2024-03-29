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

package org.limbo.flowjob.broker.application.component;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.agent.AgentRegistry;
import org.limbo.flowjob.broker.core.agent.AgentRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author Devil
 * @since 2023/9/12
 */
@Slf4j
@Component
public class AgentRegistryComponent extends AgentRegistry implements InitializingBean {

    public AgentRegistryComponent(AgentRepository agentRepository) {
        super(agentRepository);
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }

}
