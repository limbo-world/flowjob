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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.utils.SHAUtils;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.net.URL;
import java.util.Objects;

/**
 * @author Devil
 * @since 2022/7/20
 */
@Slf4j
public abstract class Broker {

    @Getter
    protected String name;

    @Getter
    protected URL rpcBaseURL;

    protected final NodeRegistry registry;

    protected final NodeManger manger;

    public Broker(String name, URL baseURL, NodeRegistry registry, NodeManger manger) {
        Objects.requireNonNull(baseURL, "URL can't be null");

        this.name = StringUtils.isBlank(name) ? SHAUtils.sha1AndHex(baseURL.toString()).toUpperCase() : name;
        this.rpcBaseURL = baseURL;
        this.registry = registry;
        this.manger = manger;
    }

    /**
     * 启动节点
     */
    public void start() {
        // 将自己先注册上去
        manger.online(new Node(name, rpcBaseURL.getHost(), rpcBaseURL.getPort()));
        // 节点注册 用于集群感知
        registry.register(name, rpcBaseURL.getHost(), rpcBaseURL.getPort());
        // 节点变更通知
        registry.subscribe(event -> {
            switch (event.getType()) {
                case ONLINE:
                    manger.online(new Node(event.getName(), event.getHost(), event.getPort()));
                    if (log.isDebugEnabled()) {
                        log.debug("[BrokerNodeListener] receive online evnet {}", JacksonUtils.toJSONString(event));
                    }
                    break;
                case OFFLINE:
                    manger.offline(new Node(event.getName(), event.getHost(), event.getPort()));
                    if (log.isDebugEnabled()) {
                        log.debug("[BrokerNodeListener] receive offline evnet {}", JacksonUtils.toJSONString(event));
                    }
                    break;
                default:
                    log.warn("[BrokerNodeListener] " + MsgConstants.UNKNOWN + " evnet {}", JacksonUtils.toJSONString(event));
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
