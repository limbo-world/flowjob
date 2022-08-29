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

package org.limbo.flowjob.broker.application.plan.support;

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
public class NodeMangerImpl implements NodeManger {

    private static final Map<String, Node> map = new ConcurrentHashMap<>();

    @Override
    public void online(Node node) {
        map.put(key(node), node);
    }

    @Override
    public void offline(Node node) {
        map.remove(key(node));
    }

    @Override
    public boolean alive(Node node) {
        return map.containsKey(key(node));
    }

    @Override
    public Collection<Node> allAlive() {
        return map.values();
    }

    private static String key(Node node) {
        return node.getHost() + ":" + node.getPort();
    }

}
