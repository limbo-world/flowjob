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

package org.limbo.flowjob.broker.application.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.AgentStatus;
import org.limbo.flowjob.broker.application.component.BrokerSlotManager;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixDelayMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;
import org.limbo.flowjob.broker.dao.entity.AgentEntity;
import org.limbo.flowjob.broker.dao.repositories.AgentEntityRepo;
import org.limbo.flowjob.common.constants.AgentConstant;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 获取plan下发
 * 相比update的任务比较久
 * 此任务主要为防止 plan 调度中异常导致 在时间轮中丢失
 */
@Slf4j
@Component
public class AgentStatusTask extends FixDelayMetaTask {

    private AgentEntityRepo agentEntityRepo;

    private BrokerSlotManager slotManager;

    private Broker broker;

    private NodeManger nodeManger;

    public AgentStatusTask(MetaTaskScheduler scheduler,
                           BrokerSlotManager slotManager,
                           AgentEntityRepo agentEntityRepo,
                           @Lazy Broker broker,
                           NodeManger nodeManger) {
        super(Duration.ofSeconds(0), Duration.ofSeconds(AgentConstant.HEARTBEAT_TIMEOUT_SECOND), scheduler);
        this.slotManager = slotManager;
        this.agentEntityRepo = agentEntityRepo;
        this.broker = broker;
        this.nodeManger = nodeManger;
    }

    /**
     * 执行元任务，从 DB 加载一批待调度的 Plan，放到调度器中去。
     */
    @Override
    protected void executeTask() {
        try {
            // 判断自己是否存在 --- 可能由于心跳异常导致不存活
            if (!nodeManger.alive(broker.getName())) {
                return;
            }

            // 调度当前时间以及未来的任务
            List<String> agentIds = slotManager.agentIds();
            if (CollectionUtils.isEmpty(agentIds)) {
                return;
            }

            List<AgentEntity> agentEntities = agentEntityRepo.findByAgentIdInAndDeleted(agentIds, false);
            if (CollectionUtils.isEmpty(agentEntities)) {
                return;
            }

            LocalDateTime now = TimeUtils.currentLocalDateTime();
            long heartbeatTimeout = AgentConstant.HEARTBEAT_TIMEOUT_SECOND;

            for (AgentEntity agentEntity : agentEntities) {
                AgentStatus currentStatus = AgentStatus.parse(agentEntity.getStatus());
                if (AgentStatus.RUNNING == currentStatus) {

                    if (agentEntity.getLastHeartbeatAt().plus(heartbeatTimeout, ChronoUnit.SECONDS).isBefore(now)) {
                        agentEntityRepo.updateStatus(agentEntity.getAgentId(), AgentStatus.RUNNING.status, AgentStatus.FUSING.status);
                    }

                } else if (AgentStatus.FUSING == currentStatus) {
                    if (agentEntity.getLastHeartbeatAt().plus(heartbeatTimeout * 2, ChronoUnit.SECONDS).isBefore(now)) {
                        agentEntityRepo.updateStatus(agentEntity.getAgentId(), AgentStatus.FUSING.status, AgentStatus.TERMINATED.status);
                    }
                }
            }

        } catch (Exception e) {
            log.error("{} load and schedule AgentStatusTask fail", scheduleId(), e);
        }
    }


    @Override
    public MetaTaskType getType() {
        return MetaTaskType.AGENT_STATUS;
    }

    @Override
    public String getMetaId() {
        return this.getClass().getSimpleName();
    }

}
