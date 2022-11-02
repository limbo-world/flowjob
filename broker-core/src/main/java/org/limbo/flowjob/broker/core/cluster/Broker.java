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
import org.limbo.flowjob.common.constants.MsgConstants;

/**
 * @author Devil
 * @since 2022/7/20
 */
@Slf4j
public abstract class Broker {

    protected final BrokerConfig config;

    protected final NodeRegistry registry;

    protected final NodeManger manger;

    public Broker(BrokerConfig config, NodeRegistry registry, NodeManger manger) {
        this.config = config;
        this.registry = registry;
        this.manger = manger;
    }

    /**
     * 启动节点
     */
    public void start() {
        // 节点注册 用于集群感知
        registry.register(config.getName(), config.getHost(), config.getPort());
        // 节点变更通知
        registry.subscribe(event -> {
            switch (event.getType()) {
                case ONLINE:
                    manger.online(new Node(event.getName(), event.getHost(), event.getPort()));
                    if (log.isDebugEnabled()) {
                        log.warn("[BrokerNodeListener] receive evnet {}", event);
                    }
                    break;
                case OFFLINE:
                    manger.offline(new Node(event.getName(), event.getHost(), event.getPort()));
                    if (log.isDebugEnabled()) {
                        log.warn("[BrokerNodeListener] receive evnet {}", event);
                    }
                    break;
                default:
                    log.warn("[BrokerNodeListener] " + MsgConstants.UNKNOWN + " evnet {}", event);
                    break;
            }
        });
        log.info("broker start!!!~~~");
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
