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

import lombok.extern.slf4j.Slf4j;

/**
 * @author Devil
 * @since 2022/7/20
 */
@Slf4j
public abstract class BrokerNode {

    protected BrokerConfig config;

    protected final BrokerRegistry registry;

    public BrokerNode(BrokerConfig config, BrokerRegistry registry) {
        this.config = config;
        this.registry = registry;
    }

    /**
     * 启动节点
     */
    public void start() {
        // 节点注册 用于集群感知
        registry.register(config.getHost(), config.getPort());
        // 节点变更通知
        registry.subscribe(event -> {
            switch (event.getType()) {
                case ONLINE:
                    BrokerNodeManger.online(event.getNodeId(), event.getHost(), event.getPort());
                    break;
                case OFFLINE:
                    BrokerNodeManger.offline(event.getHost(), event.getPort());
                    break;
                default:
                    log.warn("[BrokerNodeListener] unknown evnet {}", event);
                    break;
            }
        });
    }

    /**
     * 停止
     */
    public abstract void stop();

    /**
     * @return 是否正在运行
     */
    public abstract boolean isRunning();

    /**
     * @return 是否已停止
     */
    public abstract boolean isStopped();


}
