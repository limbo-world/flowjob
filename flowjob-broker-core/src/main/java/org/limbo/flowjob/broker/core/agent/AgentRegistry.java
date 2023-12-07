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

package org.limbo.flowjob.broker.core.agent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.AgentStatus;
import org.limbo.flowjob.common.constants.AgentConstant;
import org.limbo.flowjob.common.utils.time.Formatters;
import org.limbo.flowjob.common.utils.time.TimeFormateUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.net.URL;
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
 * @since 2023/11/21
 */
@Slf4j
public class AgentRegistry {

    private static final Map<String, ScheduleAgent> ONLINE_AGENT_MAP = new ConcurrentHashMap<>();

    private final AgentRepository agentRepository;

    /**
     * 心跳超时时间，毫秒
     */
    private final Duration heartbeatTimeout = Duration.ofSeconds(AgentConstant.HEARTBEAT_TIMEOUT_SECOND);

    public AgentRegistry(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    /**
     * 获取所有存活Agent
     */
    public Collection<ScheduleAgent> all() {
        return ONLINE_AGENT_MAP.values();
    }

    public void init() {
        new Timer().schedule(new AgentOnlineCheckTask(), 0, heartbeatTimeout.toMillis());
        new Timer().schedule(new AgentFusingCheckTask(), 0, heartbeatTimeout.toMillis());
        new Timer().schedule(new AgentTerminatedCheckTask(), 0, heartbeatTimeout.toMillis());
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
                List<ScheduleAgent> onlines = agentRepository.findByLastHeartbeatAtBetween(startTime, endTime);
                if (CollectionUtils.isNotEmpty(onlines)) {
                    for (ScheduleAgent agent : onlines) {
                        // 更新数据
                        ScheduleAgent n = ONLINE_AGENT_MAP.put(agent.getId(), agent);
                        URL url = agent.getUrl();
                        if (n == null && log.isDebugEnabled()) {
                            log.debug("{} find online id: {}, host: {}, port: {} lastHeartbeat:{}", TASK_NAME, agent.getId(), url.getHost(), url.getPort(), TimeFormateUtils.format(agent.getLastHeartbeatAt(), Formatters.YMD_HMS));
                        }
                    }
                }
                lastCheckTime = endTime;
            } catch (Exception e) {
                log.error("{} check fail", TASK_NAME, e);
            }
        }

    }

    private class AgentFusingCheckTask extends TimerTask {

        private static final String TASK_NAME = "[AgentFusingCheckTask]";

        LocalDateTime lastCheckTime = TimeUtils.currentLocalDateTime().plusSeconds(-2 * heartbeatTimeout.getSeconds());

        @Override
        public void run() {
            try {
                LocalDateTime startTime = lastCheckTime;
                LocalDateTime endTime = TimeUtils.currentLocalDateTime().plusSeconds(-heartbeatTimeout.getSeconds());
                if (log.isDebugEnabled()) {
                    log.debug("{} check start:{} end:{}", TASK_NAME, TimeFormateUtils.format(startTime, Formatters.YMD_HMS), TimeFormateUtils.format(endTime, Formatters.YMD_HMS));
                }
                List<ScheduleAgent> offlines = agentRepository.findByLastHeartbeatAtBetween(startTime, endTime);
                if (CollectionUtils.isNotEmpty(offlines)) {
                    for (ScheduleAgent agent : offlines) {
                        URL url = agent.getUrl();
                        if (log.isDebugEnabled()) {
                            log.debug("{} find id: {}, host: {}, port: {} lastHeartbeat:{}", TASK_NAME, agent.getId(), url.getHost(), url.getPort(), TimeFormateUtils.format(agent.getLastHeartbeatAt(), Formatters.YMD_HMS));
                        }
                        ONLINE_AGENT_MAP.remove(agent.getId());

                        if (AgentStatus.RUNNING == agent.getStatus()) {
                            agentRepository.updateStatus(agent.getId(), AgentStatus.RUNNING.status, AgentStatus.FUSING.status);
                        }
                    }
                }
                lastCheckTime = endTime;
            } catch (Exception e) {
                log.error("{} check fail", TASK_NAME, e);
            }
        }
    }

    private class AgentTerminatedCheckTask extends TimerTask {

        private static final String TASK_NAME = "[AgentTerminatedCheckTask]";

        LocalDateTime lastCheckTime = TimeUtils.currentLocalDateTime().plusSeconds(-3 * heartbeatTimeout.getSeconds());

        @Override
        public void run() {
            try {
                LocalDateTime startTime = lastCheckTime;
                LocalDateTime endTime = TimeUtils.currentLocalDateTime().plusSeconds(-2 * heartbeatTimeout.getSeconds());
                if (log.isDebugEnabled()) {
                    log.debug("{} check start:{} end:{}", TASK_NAME, TimeFormateUtils.format(startTime, Formatters.YMD_HMS), TimeFormateUtils.format(endTime, Formatters.YMD_HMS));
                }
                List<ScheduleAgent> offlines = agentRepository.findByLastHeartbeatAtBetween(startTime, endTime);
                if (CollectionUtils.isNotEmpty(offlines)) {
                    for (ScheduleAgent agent : offlines) {
                        URL url = agent.getUrl();
                        if (log.isDebugEnabled()) {
                            log.debug("{} find id: {}, host: {}, port: {} lastHeartbeat:{}", TASK_NAME, agent.getId(), url.getHost(), url.getPort(), TimeFormateUtils.format(agent.getLastHeartbeatAt(), Formatters.YMD_HMS));
                        }
                        ONLINE_AGENT_MAP.remove(agent.getId());

                        if (AgentStatus.FUSING == agent.getStatus()) {
                            agentRepository.updateStatus(agent.getId(), AgentStatus.FUSING.status, AgentStatus.TERMINATED.status);
                        }
                    }
                }
                lastCheckTime = endTime;
            } catch (Exception e) {
                log.error("{} check fail", TASK_NAME, e);
            }
        }
    }

}
