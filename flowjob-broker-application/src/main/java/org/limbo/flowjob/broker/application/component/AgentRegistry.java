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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.dao.entity.AgentEntity;
import org.limbo.flowjob.broker.dao.repositories.AgentEntityRepo;
import org.limbo.flowjob.common.constants.AgentConstant;
import org.limbo.flowjob.common.utils.time.Formatters;
import org.limbo.flowjob.common.utils.time.TimeFormateUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Devil
 * @since 2023/9/12
 */
@Slf4j
@Component
public class AgentRegistry implements InitializingBean {

    private static final Map<String, AgentEntity> map = new ConcurrentHashMap<>();

    @Setter(onMethod_ = @Inject)
    private AgentEntityRepo agentEntityRepo;

    /**
     * 心跳超时时间，毫秒
     */
    private Duration heartbeatTimeout = Duration.ofSeconds(AgentConstant.HEARTBEAT_TIMEOUT_SECOND);

    @Override
    public void afterPropertiesSet() throws Exception {
        new Timer().schedule(new AgentOnlineCheckTask(), 0, heartbeatTimeout.toMillis());
        new Timer().schedule(new AgentOfflineCheckTask(), 0, heartbeatTimeout.toMillis());
    }

    public Collection<AgentEntity> all() {
        return map.values();
    }

    private class AgentOnlineCheckTask extends TimerTask {

        private static final String TASK_NAME = "[AgentOnlineCheckTask]";

        LocalDateTime lastCheckTime = TimeUtils.currentLocalDateTime().plusSeconds(-heartbeatTimeout.getSeconds());

        @Override
        public void run() {
            try {
                LocalDateTime startTime = lastCheckTime;
                LocalDateTime endTime = TimeUtils.currentLocalDateTime();
                if (log.isDebugEnabled()) {
                    log.info("{} checkOnline start:{} end:{}", TASK_NAME, TimeFormateUtils.format(startTime, Formatters.YMD_HMS), TimeFormateUtils.format(endTime, Formatters.YMD_HMS));
                }
                List<AgentEntity> onlines = agentEntityRepo.findByLastHeartbeatAtBetween(startTime, endTime);
                if (CollectionUtils.isNotEmpty(onlines)) {
                    for (AgentEntity agent : onlines) {
                        AgentEntity n = map.putIfAbsent(agent.getAgentId(), agent);
                        if (n == null && log.isDebugEnabled()) {
                            log.debug("{} find online id: {}, host: {}, port: {} lastHeartbeat:{}", TASK_NAME, agent.getAgentId(), agent.getHost(), agent.getPort(), TimeFormateUtils.format(agent.getLastHeartbeatAt(), Formatters.YMD_HMS));
                        }
                    }
                }
                lastCheckTime = endTime;
            } catch (Exception e) {
                log.error("{} check fail", TASK_NAME, e);
            }
        }

    }

    private class AgentOfflineCheckTask extends TimerTask {

        private static final String TASK_NAME = "[AgentOfflineCheckTask]";

        LocalDateTime lastCheckTime = TimeUtils.currentLocalDateTime().plusSeconds(-2 * heartbeatTimeout.getSeconds());

        @Override
        public void run() {
            try {
                LocalDateTime startTime = lastCheckTime;
                LocalDateTime endTime = TimeUtils.currentLocalDateTime().plusSeconds(-heartbeatTimeout.getSeconds());
                if (log.isDebugEnabled()) {
                    log.debug("{} checkOffline start:{} end:{}", TASK_NAME, TimeFormateUtils.format(startTime, Formatters.YMD_HMS), TimeFormateUtils.format(endTime, Formatters.YMD_HMS));
                }
                List<AgentEntity> offlines = agentEntityRepo.findByLastHeartbeatAtBetween(startTime, endTime);
                if (CollectionUtils.isNotEmpty(offlines)) {
                    for (AgentEntity agent : offlines) {
                        if (log.isDebugEnabled()) {
                            log.debug("{} find offline id: {}, host: {}, port: {} lastHeartbeat:{}", TASK_NAME, agent.getAgentId(), agent.getHost(), agent.getPort(), TimeFormateUtils.format(agent.getLastHeartbeatAt(), Formatters.YMD_HMS));
                        }
                        map.remove(agent.getAgentId());
                    }
                }
                lastCheckTime = endTime;
            } catch (Exception e) {
                log.error("{} check fail", TASK_NAME, e);
            }
        }
    }

}
