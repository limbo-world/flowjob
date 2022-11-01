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

/**
 * @author Devil
 * @since 2022/7/21
 */
@Data
public class BrokerConfig {

    /**
     * broker的唯一标识
     */
    private String name;

    /**
     * 提供给worker的服务的 host
     */
    private String host;

    /**
     * 提供给worker的服务 port
     */
    private Integer port;

    /**
     * 心跳时间间隔
     */
    private long heartbeatInterval = 2000;

    /**
     * 心跳超时时间
     */
    private long heartbeatTimeout = 10000;

}
