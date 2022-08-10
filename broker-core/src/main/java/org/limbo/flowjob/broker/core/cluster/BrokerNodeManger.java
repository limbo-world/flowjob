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

package org.limbo.flowjob.broker.core.cluster;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * todo
 * @author Devil
 * @since 2022/7/20
 */
public class BrokerNodeManger {
    // key: host:port  ----   id
    private static final Map<String, String> map = new ConcurrentHashMap<>();

    /**
     * 节点上线
     */
    public static void online(String id, String host, int port) {
        String key = key(host, port);
        map.put(key, id);
    }

    /**
     * 节点下线
     * @param host
     * @param port
     */
    public static void offline(String host, int port) {
        String key = key(host, port);
        map.remove(key);
    }

    /**
     * 检查节点是否存活
     * @param host
     * @param port
     * @return
     */
    public static boolean alive(String host, int port) {
        String key = key(host, port);
        return map.containsKey(key);
    }

    public static Collection<String> ids() {
        return map.values();
    }

    public static List<Pair<String, Integer>> alive() {
        List<Pair<String, Integer>> result = new ArrayList<>();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            result.add(pair(key));
        }
        return result;
    }

    private static String key(String host, int port) {
        return host + ":" + port;
    }

    private static Pair<String, Integer> pair(String key) {
        String[] split = key.split(":");
        return Pair.of(split[0], Integer.valueOf(split[1]));
    }

}
