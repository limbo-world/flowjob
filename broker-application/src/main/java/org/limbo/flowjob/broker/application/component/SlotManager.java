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
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.Node;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2022/8/22
 */
@Slf4j
@Component
public class SlotManager {

    @Setter(onMethod_ = @Inject)
    private BrokerConfig brokerConfig;

    @Setter(onMethod_ = @Inject)
    private NodeManger nodeManger;

    @Setter(onMethod_ = @Inject)
    private PlanSlotEntityRepo planSlotEntityRepo;

    public static final int SLOT_SIZE = 64;

    /**
     * 计算槽位
     */
    public int slot(String planId) {
        return planId.hashCode() % SlotManager.SLOT_SIZE;
    }

    /**
     * 获取槽位的算法
     *
     * @return 当前机器对应的所有槽位
     */
    public List<Integer> slots() {
        List<Node> sortedNodes = nodeManger.allAlive().stream().sorted(Comparator.comparing(Node::getHost).thenComparingInt(Node::getPort)).collect(Collectors.toList());

        // 判断自己所在的id位置
        int mark = -1;
        for (int i = 0; i < sortedNodes.size(); i++) {
            Node node = sortedNodes.get(i);
            if (Objects.equals(brokerConfig.getHost(), node.getHost()) && Objects.equals(brokerConfig.getPort(), node.getPort())) {
                mark = i;
                break;
            }
        }

        if (mark < 0) {
            log.warn("can't find in alive nodes host:{} port:{}", brokerConfig.getHost(), brokerConfig.getPort());
            return Collections.emptyList();
        }

        List<Integer> slots = new ArrayList<>();
        while (mark < SLOT_SIZE) {
            slots.add(mark);
            mark += sortedNodes.size();
        }
        log.info("find slots:{}", slots);
        return slots;
    }

    /**
     * 获取当前节点对应的planId
     */
    public List<String> planIds() {
        List<Integer> slots = slots();
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

}
