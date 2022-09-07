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

package org.limbo.flowjob.worker.starter.configuration;

import org.limbo.flowjob.broker.api.constants.enums.WorkerProtocol;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.worker.core.domain.CalculatingWorkerResource;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.core.domain.WorkerResources;
import org.limbo.flowjob.worker.core.rpc.BrokerNode;
import org.limbo.flowjob.worker.core.rpc.BrokerRpc;
import org.limbo.flowjob.worker.core.rpc.OkHttpBrokerRpc;
import org.limbo.flowjob.worker.core.rpc.lb.BaseLoadBalancer;
import org.limbo.flowjob.worker.core.rpc.lb.LBStrategy;
import org.limbo.flowjob.worker.core.rpc.lb.LoadBalancer;
import org.limbo.flowjob.worker.core.rpc.lb.RoundRobinStrategy;
import org.limbo.flowjob.worker.starter.processor.ExecutorMethodProcessor;
import org.limbo.flowjob.worker.starter.properties.WorkerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-09-05
 */
@Configuration
@ConditionalOnProperty(prefix = "flowjob.worker", value = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WorkerProperties.class)
public class WorkerAutoConfiguration {

    private final WorkerProperties workerProps;

    public WorkerAutoConfiguration(WorkerProperties workerProps) {
        this.workerProps = workerProps;
    }


    /**
     * 用于扫描 @Executor 注解标记的方法
     */
    @Bean
    public ExecutorMethodProcessor executorMethodProcessor() {
        return new ExecutorMethodProcessor();
    }


    /**
     * Worker 实例，
     * @param rpc broker rpc 通信模块
     */
    @Bean
    public Worker httpWorker(WorkerResources resources, BrokerRpc rpc) throws MalformedURLException {
        URL workerBaseUrl = workerProps.getPort() > 0
                ? new URL(workerProps.getScheme().name(), workerProps.getHost(), workerProps.getPort(), "")
                : new URL(workerProps.getScheme().name(), workerProps.getHost(), "");

        return new Worker(workerProps.getId(), workerBaseUrl, resources, rpc);
    }


    /**
     * 动态计算 Worker 资源
     */
    @Bean
    @ConditionalOnMissingBean(WorkerResources.class)
    public WorkerResources calculatingWorkerResource() {
        return new CalculatingWorkerResource(workerProps.getTaskConcurrency(), workerProps.getTaskQueueSize());
    }


    /**
     * Broker 通信模块
     */
    @Bean
    @ConditionalOnMissingBean(BrokerRpc.class)
    public BrokerRpc brokerRpc(LoadBalancer<BrokerNode> brokerLoadBalancer) {
        List<URL> brokers = workerProps.getBrokers();
        Verifies.notEmpty(brokers, "No brokers configured");

        // HTTP、HTTPS 协议
        String brokerProtocol = brokers.get(0).getProtocol();
        if (WorkerProtocol.HTTP.is(brokerProtocol) || WorkerProtocol.HTTPS.is(brokerProtocol)) {
            return httpBrokerRpc(new HashSet<>(brokers), brokerLoadBalancer);
        }

        throw new IllegalArgumentException("Unsupported broker protocol [" + brokerProtocol + "]");
    }


    /**
     * HTTP 协议的 broker 通信
     */
    private OkHttpBrokerRpc httpBrokerRpc(Set<URL> brokers, LoadBalancer<BrokerNode> loadBalancer) {
        List<BrokerNode> brokerNodes = brokers.stream()
                .map(BrokerNode::new)
                .collect(Collectors.toList());
        return new OkHttpBrokerRpc(brokerNodes, loadBalancer);
    }


    /**
     * Broker 负载均衡器
     */
    @Bean("brokerLoadBalancer")
    @ConditionalOnMissingBean(name = "brokerLoadBalancer")
    public LoadBalancer<BrokerNode> brokerLoadBalancer(LBStrategy<BrokerNode> lbStrategy) {
        return new BaseLoadBalancer<>("brokerLoadBalancer", lbStrategy);
    }


    /**
     * Broker 负载均衡策略
     */
    @Bean("brokerLoadBalanceStrategy")
    @ConditionalOnMissingBean(name = "brokerLoadBalanceStrategy")
    public LBStrategy<BrokerNode> brokerLoadBalanceStrategy() {
        return new RoundRobinStrategy<>();
    }

}
