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

package org.limbo.flowjob.agent.starter.properties;

import lombok.Data;
import org.limbo.flowjob.api.constants.Protocol;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;
import java.time.Duration;
import java.util.List;

/**
 * @author Brozen
 * @since 2022-09-05
 */
@Data
@ConfigurationProperties(prefix = "flowjob.agent")
public class AgentProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 是否自动启动 agent
     */
    private boolean autoStart = true;

    /**
     * 节点连接列表
     */
    private List<URL> brokers;

    /**
     * 注册时，向 broker 提交的 RPC 通信协议类型。默认为 http。
     */
    private Protocol protocol = Protocol.HTTP;

    /**
     * worker 注册时，向 broker 提交的 RPC 通信 host，可以是域名或 IP 地址，如不填写则自动发现本机非 127.0.0.1 的地址。
     * 多网卡场景下，建议显式配置 host。
     */
    private String host = "";

    /**
     * worker 注册时，向 broker 提交的 RPC 通信端口，默认为 null。
     * 如果未指定此配置，则尝试使用 ${server.port} 配置；如 ${server.port} 配置也不存在，则使用 8080，
     */
    private Integer port = null;

    /**
     * Worker 向 Broker 发送心跳请求的间隔，默认 2 秒。
     */
    private Duration heartbeat = Duration.ofSeconds(2);

    /**
     * 任务执行并发数量。允许同时执行的任务个数，同时执行的任务数量超出此限制后，后续接收的任务将放入积压队列中。默认为系统 CPU 核数。
     */
    private int concurrency = Runtime.getRuntime().availableProcessors();

    /**
     * 任务积压队列容量。如积压队列已满，则无法继续接收任务。为0情况下队列数大小等于任务执行并发数。
     */
    private int queueSize = 1024;

    /**
     * 数据连接地址
     */
    private String datasourceUrl;

    private String datasourceUsername;

    private String datasourcePassword;

    /**
     * 是否初始化数据库 如果是持久化 task 的则选 false 交由运维管理
     */
    private boolean initTable;


}
