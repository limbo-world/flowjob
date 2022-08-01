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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * todo
 * @author Devil
 * @since 2022/7/20
 */
class BrokerNodeManger {

    private static final Map<String, String> map = new ConcurrentHashMap<>();

    public static void online(String host, int port) {
        String key = key(host, port);
        map.put(key, key);
    }

    public static void offline(String host, int port) {
        String key = key(host, port);
        map.remove(key);
    }

    public static boolean alive(String host, int port) {
        String key = key(host, port);
        return map.containsKey(key);
    }


    private static String key(String host, int port) {
        return host + ":" + port;
    }

}
