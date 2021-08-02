/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.start.shared;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Data
@ConfigurationProperties(prefix = "flowjob.worker")
public class WorkerProperties {
    /**
     * 节点名称
     */
    private String name;
    /**
     * 队列容量
     */
    private int queueSize;
    /**
     * tracker host
     */
    private String serverHost;
    /**
     * tracker port
     */
    private int serverPort;
    /**
     * 当前 worker host 可为空
     */
    private String localHost;
    /**
     * 当前 worker port
     */
    private int localPort;
    /**
     * 节点标签
     */
    private List<String> tags;
}
