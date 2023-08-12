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

package org.limbo.flowjob.agent.starter;

import lombok.experimental.Delegate;
import org.limbo.flowjob.agent.ScheduleAgent;
import org.limbo.flowjob.agent.starter.component.AgentStartEvent;
import org.limbo.flowjob.agent.starter.properties.AgentProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

/**
 * @author Brozen
 * @since 2022-09-11
 */
public class SpringDelegatedAgent implements ScheduleAgent, SmartInitializingSingleton, ApplicationEventPublisherAware, DisposableBean {

    @Delegate(types = ScheduleAgent.class)
    private final ScheduleAgent delegated;

    @Resource
    private AgentProperties properties;

    private ApplicationEventPublisher eventPublisher;


    public SpringDelegatedAgent(ScheduleAgent delegated) {
        this.delegated = delegated;
    }


    /**
     * Bean 销毁时，停止 Worker
     */
    @Override
    public void destroy() {
        delegated.stop();
    }

    @Override
    public void setApplicationEventPublisher(@Nonnull ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (properties.isAutoStart()) {
            eventPublisher.publishEvent(new AgentStartEvent());
        }
    }

    /**
     * 监听到 AgentStartEvent 事件后，启动
     */
    @EventListener(AgentStartEvent.class)
    public void onReady(AgentStartEvent event) {
        delegated.start(properties.getHeartbeat());
    }
}
