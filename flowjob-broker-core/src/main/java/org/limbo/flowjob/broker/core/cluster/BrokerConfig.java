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

import lombok.Data;
import org.limbo.flowjob.api.constants.Protocol;

/**
 * @author Devil
 * @since 2022/7/21
 */
@Data
public class BrokerConfig {

    /**
     * 是否启动 broker
     */
    private boolean enabled = true;

    /**
     * broker的唯一标识，建议不配置
     */
    protected String name;

    /**
     * 提供给worker的服务的 host。可以是域名或 IP 地址，如不填写则自动发现本机非 127.0.0.1 的地址。
     * 多网卡场景下，建议显式配置 host。
     */
    protected String host;

    /**
     * 提供给worker的服务 port
     * 如果未指定此配置，则尝试使用 ${server.port} 配置；如 ${server.port} 配置也不存在，则使用 8080，
     */
    protected Integer port;

    /**
     * RPC 通信协议类型。默认为 http。
     */
    private Protocol protocol = Protocol.HTTP;

    /**
     * 心跳时间间隔，毫秒
     */
    protected long heartbeatInterval = 2000;

    /**
     * 心跳超时时间，毫秒
     */
    protected long heartbeatTimeout = 5000;

}
