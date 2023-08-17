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

import org.limbo.flowjob.broker.core.cluster.Node;
import org.limbo.flowjob.broker.core.cluster.NodeManger;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存中缓存的 broker节点信息
 *
 * @author Devil
 * @since 2022/7/20
 */
public class LocalNodeManger implements NodeManger {

    private static final Map<String, Node> map = new ConcurrentHashMap<>();

    private BrokerSlotManager slotManager;

    public LocalNodeManger(BrokerSlotManager slotManager) {
        this.slotManager = slotManager;
    }

    @Override
    public void online(Node node) {
        map.putIfAbsent(node.getName(), node);
        slotManager.rehash(map.values());
    }

    @Override
    public void offline(Node node) {
        Node rn = map.remove(node.getName());
        if (rn != null) {
            slotManager.rehash(map.values());
        }
    }

    @Override
    public boolean alive(String name) {
        return map.containsKey(name);
    }

    @Override
    public Collection<Node> allAlive() {
        return map.values();
    }

}
