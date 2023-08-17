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
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.cluster.Node;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.broker.dao.entity.AgentSlotEntity;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerSlotEntity;
import org.limbo.flowjob.broker.dao.repositories.AgentSlotEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.WorkerSlotEntityRepo;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2022/8/22
 */
@Slf4j
public class BrokerSlotManager {

    private PlanSlotEntityRepo planSlotEntityRepo;

    private WorkerSlotEntityRepo workerSlotEntityRepo;

    private AgentSlotEntityRepo agentSlotEntityRepo;

    private static final int SLOT_SIZE = 1024;

    private final String host;

    private final int port;

    private Set<Integer> slots;

    public BrokerSlotManager(String host, int port,
                             PlanSlotEntityRepo planSlotEntityRepo,
                             WorkerSlotEntityRepo workerSlotEntityRepo,
                             AgentSlotEntityRepo agentSlotEntityRepo) {
        this.host = host;
        this.port = port;
        this.planSlotEntityRepo = planSlotEntityRepo;
        this.workerSlotEntityRepo = workerSlotEntityRepo;
        this.agentSlotEntityRepo = agentSlotEntityRepo;

        this.slots = Collections.emptySet();
    }

    /**
     * 计算槽位
     */
    public int slot(String id) {
        return id.hashCode() % SLOT_SIZE;
    }

    /**
     * 获取当前节点对应的planId
     */
    public List<String> planIds() {
        if (CollectionUtils.isEmpty(slots)) {
            return Collections.emptyList();
        }
        List<PlanSlotEntity> slotEntities = planSlotEntityRepo.findBySlotIn(slots);
        if (CollectionUtils.isEmpty(slotEntities)) {
            return Collections.emptyList();
        }

        return slotEntities.stream()
                .map(PlanSlotEntity::getPlanId)
                .collect(Collectors.toList());
    }

    public void checkPlanId(String planId) {
        Verifies.notEmpty(slots, "slots is empty");
        PlanSlotEntity planSlotEntity = planSlotEntityRepo.findByPlanId(planId);
        Verifies.notNull(planSlotEntity, "plan's slot is null id:" + planId);
        Verifies.verify(slots.contains(planSlotEntity.getSlot()), MessageFormat.format("plan {0} is not in this broker", planId));
    }

    /**
     * 获取当前节点对应的workerId
     */
    public List<String> workerIds() {
        if (CollectionUtils.isEmpty(slots)) {
            return Collections.emptyList();
        }
        List<WorkerSlotEntity> slotEntities = workerSlotEntityRepo.findBySlotIn(slots);
        if (CollectionUtils.isEmpty(slotEntities)) {
            return Collections.emptyList();
        }

        return slotEntities.stream()
                .map(WorkerSlotEntity::getWorkerId)
                .collect(Collectors.toList());
    }

    /**
     * 获取当前节点对应的agentId
     */
    public List<String> agentIds() {
        if (CollectionUtils.isEmpty(slots)) {
            return Collections.emptyList();
        }
        List<AgentSlotEntity> slotEntities = agentSlotEntityRepo.findBySlotIn(slots);
        if (CollectionUtils.isEmpty(slotEntities)) {
            return Collections.emptyList();
        }

        return slotEntities.stream()
                .map(AgentSlotEntity::getAgentId)
                .collect(Collectors.toList());
    }

    /**
     * 获取槽位的算法
     *
     * @return 当前机器对应的所有槽位
     */
    public synchronized void rehash(Collection<Node> nodes) {
        List<Node> sortedNodes = nodes.stream().sorted(Comparator.comparing(Node::getHost).thenComparingInt(Node::getPort)).collect(Collectors.toList());

        // 判断自己所在的id位置
        int mark = -1;
        for (int i = 0; i < sortedNodes.size(); i++) {
            Node node = sortedNodes.get(i);
            if (Objects.equals(host, node.getHost()) && Objects.equals(port, node.getPort())) {
                mark = i;
                break;
            }
        }

        if (mark < 0) {
            log.warn("can't find in alive nodes host:{} port:{}", host, port);
            return;
        }

        slots = new HashSet<>();
        while (mark < SLOT_SIZE) {
            slots.add(mark);
            mark += sortedNodes.size();
        }
        log.info("find slots:{}", slots);
    }


}
