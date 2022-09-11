/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.worker.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;
import java.time.Duration;
import java.util.List;

/**
 * @author Brozen
 * @since 2022-09-05
 */
@Data
@ConfigurationProperties(prefix = "flowjob.worker")
public class WorkerProperties {

    /**
     * 是否启用 worker
     */
    private boolean enabled = true;

    /**
     * worker 节点 id，如不指定，将在 worker 启动时随机生成一个 id
     */
    private String id;

    /**
     * broker 节点连接列表
     */
    private List<URL> brokers;

    /**
     * worker 注册时，向 broker 提交的 RPC 通信协议类型。默认为 http。
     */
    private WorkerScheme scheme = WorkerScheme.http;

    /**
     * worker 注册时，向 broker 提交的 RPC 通信 host，可以是域名或 IP 地址，如不填写则自动发现本机非 127.0.0.1 的地址。
     * 多网卡场景下，建议显式配置 host。
     */
    private String host = "127.0.0.1";

    /**
     * worker 注册时，向 broker 提交的 RPC 通信端口，默认为 8080。
     */
    private int port = 8080;

    /**
     * Worker 向 Broker 发送心跳请求的间隔，默认 5 秒。
     */
    private Duration heartbeat = Duration.ofSeconds(5);

    /**
     * 任务执行并发数量。worker 将允许同时执行的任务个数，同时执行的任务数量超出此限制后，后续接收的任务将放入积压队列中。默认为系统 CPU 核数。
     */
    private int taskConcurrency = Runtime.getRuntime().availableProcessors();

    /**
     * 任务积压队列容量。如积压队列已满，则 worker 无法继续接收任务。默认积压队列为 0，即不启用积压队列。
     */
    private int taskQueueSize = 0;

    /**
     * worker 节点标签，可用于下发任务时进行过滤。
     */
    private List<String> tags;


}
