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

package org.limbo.flowjob.common.lb.strategies;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.common.lb.AbstractLBStrategy;
import org.limbo.flowjob.common.lb.Invocation;
import org.limbo.flowjob.common.lb.LBServer;
import org.limbo.flowjob.common.utils.MD5Utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Brozen
 * @since 2022-09-02
 */
@Slf4j
public class ConsistentHashLBStrategy<S extends LBServer> extends AbstractLBStrategy<S> {

    /**
     * 通过某个参数进行 Hash 计算
     */
    public static final String HASH_PARAM_NAME = "consistentHash.hashParamName";

    /**
     * 一致性哈希算法中，计算 LBServer 虚拟节点时的分片数量。默认 160。
     */
    @Setter
    private int replicas = 160;


    private final ConcurrentHashMap<String, ConsistentHashSelector<S>> selectors = new ConcurrentHashMap<>();


    /**
     * {@inheritDoc}
     * @param servers 非空
     * @param invocation
     * @return
     */
    @Override
    protected Optional<S> doSelect(List<S> servers, Invocation invocation) {
        String targetId = invocation.getInvokeTargetId();
        ConsistentHashSelector<S> selector = selectors.get(targetId);
        int serversHashcode = servers.hashCode();
        if (selector == null || selector.serversHashCode != serversHashcode) {
            selector = new ConsistentHashSelector<>(servers, serversHashcode);
            this.selectors.putIfAbsent(targetId, selector);
        }

        return selector.select(invocation);
    }


    /**
     * 一致性哈希选择器，将一组服务传递给此选择器，选择器将为每个服务生成 {@link ConsistentHashLBStrategy#replicas} 个虚拟节点。
     * 选择时，根据 {@link Invocation#hashCode()} 选择一个虚拟节点。
     */
    class ConsistentHashSelector<SERVER extends LBServer> {

        private final TreeMap<Long, SERVER> virtualServers;

        private final int serversHashCode;

        public ConsistentHashSelector(List<SERVER> servers, int serversHashCode) {
            this.virtualServers = new TreeMap<>();
            this.serversHashCode = serversHashCode;

            for (SERVER server : servers) {
                // MD5 签名结果，每 32 位作为一组计算 slot，32 位对应 4 个字节，因此除以 4
                String sid = server.getServerId();
                for (int i = 0; i < replicas / 4; i++) {
                    byte[] digest = MD5Utils.bytes(sid + i);
                    for (int j = 0; j < 4; j++) {
                        long slot = hash(digest, j);
                        this.virtualServers.put(slot, server);
                    }
                }
            }
        }


        /**
         * 选择一个服务，根据入参 Invocation 的 hashcode 计算。
         */
        public Optional<SERVER> select(Invocation invocation) {
            Map<String, String> parameters = invocation.getLBParameters();
            String paramName = parameters.get(HASH_PARAM_NAME);
            Object hashObj = StringUtils.isNotBlank(paramName) ? parameters.get(paramName) : null;
            hashObj = hashObj == null ? invocation : hashObj;

            long hashcode = hashObj.hashCode();
            Map.Entry<Long, SERVER> entry = virtualServers.ceilingEntry(hashcode);
            if (entry == null) {
                entry = virtualServers.firstEntry();
            }

            return Optional.ofNullable(entry.getValue());
        }



        private long hash(byte[] digest, int number) {
            return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                    | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                    | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                    | (digest[number * 4] & 0xFF))
                    & 0xFFFFFFFFL;
        }

    }

}
