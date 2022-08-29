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

package org.limbo.flowjob.broker.dao.domain;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.cluster.Node;

import java.util.ArrayList;
import java.util.Collection;
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
public class SlotManager {

    private static final int SLOT_SIZE = 64;

    /**
     * 计算槽位
     */
    public static int slot(long planId) {
        return (int) (planId % SlotManager.SLOT_SIZE);
    }

    /**
     * 获取槽位的算法
     *
     * @return 当前机器对应的所有槽位
     */
    public static List<Integer> slots(Collection<Node> aliveNodes, String host, Integer port) {
        List<Node> sortedNodes = aliveNodes.stream().sorted(Comparator.comparing(Node::getHost).thenComparingInt(Node::getPort)).collect(Collectors.toList());

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

}
