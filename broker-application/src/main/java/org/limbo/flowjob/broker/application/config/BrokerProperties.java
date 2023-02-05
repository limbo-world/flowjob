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

package org.limbo.flowjob.broker.application.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Devil
 * @since 2021/7/30
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "flowjob.broker")
public class BrokerProperties extends BrokerConfig {

    /**
     * 是否启动 broker
     */
    private boolean enabled = true;

    /**
     * 重分配间隔 毫秒
     */
    private long rebalanceInterval = 10000;
    /**
     * 状态检查间隔 毫秒
     */
    private long statusCheckInterval = 10000;

}
