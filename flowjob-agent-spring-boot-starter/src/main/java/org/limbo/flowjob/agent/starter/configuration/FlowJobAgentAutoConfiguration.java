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

package org.limbo.flowjob.agent.starter.configuration;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.BaseScheduleAgent;
import org.limbo.flowjob.agent.ScheduleAgent;
import org.limbo.flowjob.agent.dispatch.WorkerSelectorFactory;
import org.limbo.flowjob.agent.rpc.AgentBrokerRpc;
import org.limbo.flowjob.agent.rpc.http.OkHttpAgentBrokerRpc;
import org.limbo.flowjob.agent.starter.SpringDelegatedAgent;
import org.limbo.flowjob.agent.starter.properties.AgentProperties;
import org.limbo.flowjob.agent.worker.SingletonWorkerStatisticsRepo;
import org.limbo.flowjob.agent.worker.WorkerStatisticsRepository;
import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.common.lb.BaseLBServer;
import org.limbo.flowjob.common.lb.BaseLBServerRepository;
import org.limbo.flowjob.common.lb.LBServerRepository;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;
import org.limbo.flowjob.common.utils.NetUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-09-05
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.ANY)
@ConditionalOnProperty(prefix = "flowjob.agent", value = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AgentProperties.class)
@ComponentScan(basePackages = "org.limbo.flowjob.agent.starter.application")
public class FlowJobAgentAutoConfiguration {

    private final AgentProperties properties;

    @Setter(onMethod_ = @Value("${server.port:8080}"))
    private Integer httpServerPort = null;

    public FlowJobAgentAutoConfiguration(AgentProperties properties) {
        this.properties = properties;
    }


    /**
     * agent 实例，
     * @param rpc broker rpc 通信模块
     */
    @Bean
    public ScheduleAgent httpWorker(AgentBrokerRpc rpc) throws MalformedURLException {
        // 优先使用 SpringMVC 或 SpringWebflux 设置的端口号
        Integer port = properties.getPort() != null ? properties.getPort() : httpServerPort;

        // 优先使用指定的 host，如未指定则自动寻找本机 IP
        String host = properties.getHost();
        if (StringUtils.isEmpty(host)) {
            host = NetUtils.getLocalIp();
        }

        Assert.isTrue(port > 0, "Worker port must be a positive integer in range 1 ~ 65534");
        URL workerBaseUrl = new URL(properties.getProtocol().getValue(), host, port, "");
        ScheduleAgent agent = new BaseScheduleAgent(workerBaseUrl, rpc);
        return new SpringDelegatedAgent(agent);
    }


    /**
     * Broker 通信模块
     */
    @Bean
    @ConditionalOnMissingBean(AgentBrokerRpc.class)
    public AgentBrokerRpc brokerRpc(LBServerRepository<BaseLBServer> brokerLoadBalancer, LBStrategy<BaseLBServer> strategy) {
        List<URL> brokers = properties.getBrokers();
        if (CollectionUtils.isEmpty(brokers)) {
            throw new IllegalArgumentException("No brokers configured");
        }

        // HTTP、HTTPS 协议
        String brokerProtocol = brokers.get(0).getProtocol();
        if (Protocol.parse(brokerProtocol) == Protocol.UNKNOWN) {
            throw new IllegalArgumentException("Unsupported broker protocol [" + brokerProtocol + "]");
        }

        return httpBrokerRpc(brokerLoadBalancer, strategy);
    }


    /**
     * HTTP 协议的 broker 通信
     */
    private OkHttpAgentBrokerRpc httpBrokerRpc(LBServerRepository<BaseLBServer> loadBalancer, LBStrategy<BaseLBServer> strategy) {
        return new OkHttpAgentBrokerRpc(loadBalancer, strategy);
    }

    private List<BaseLBServer> brokerNodes() {
        List<URL> brokerUrls = properties.getBrokers() == null ? Collections.emptyList() : properties.getBrokers();
        return brokerUrls.stream()
                .map(BaseLBServer::new)
                .collect(Collectors.toList());
    }


    /**
     * Broker 仓储
     */
    @Bean("brokerLoadBalanceRepo")
    @ConditionalOnMissingBean(name = "brokerLoadBalanceRepo")
    public LBServerRepository<BaseLBServer> brokerLoadBalanceRepo() {
        return new BaseLBServerRepository<>(brokerNodes());
    }


    /**
     * Broker 负载均衡策略
     */
    @Bean("brokerLoadBalanceStrategy")
    @ConditionalOnMissingBean(name = "brokerLoadBalanceStrategy")
    public LBStrategy<BaseLBServer> brokerLoadBalanceStrategy() {
        return new RoundRobinLBStrategy<>();
    }

    /**
     * 如果未声明 WorkerStatisticsRepository 类型的 Bean，则使用基于内存统计的单机模式
     */
    @Bean
    @ConditionalOnMissingBean(WorkerStatisticsRepository.class)
    public WorkerStatisticsRepository workerStatisticsRepository() {
        return new SingletonWorkerStatisticsRepo();
    }

    /**
     * 用于生成 Worker 选择器，内部封装了 LB 算法的调用。
     */
    @Bean
    public WorkerSelectorFactory workerSelectorFactory(WorkerStatisticsRepository statisticsRepository) {
        WorkerSelectorFactory factory = new WorkerSelectorFactory();
        factory.setLbServerStatisticsProvider(statisticsRepository);
        return factory;
    }

}
