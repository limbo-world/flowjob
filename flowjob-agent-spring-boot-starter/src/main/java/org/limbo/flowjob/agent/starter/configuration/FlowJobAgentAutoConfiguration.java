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

package org.limbo.flowjob.agent.starter.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.core.AgentResources;
import org.limbo.flowjob.agent.core.BaseAgentResources;
import org.limbo.flowjob.agent.core.BaseScheduleAgent;
import org.limbo.flowjob.agent.core.FlowjobConnectionFactory;
import org.limbo.flowjob.agent.core.ScheduleAgent;
import org.limbo.flowjob.agent.core.TaskDispatcher;
import org.limbo.flowjob.agent.core.repository.JobRepository;
import org.limbo.flowjob.agent.core.repository.TaskRepository;
import org.limbo.flowjob.agent.core.rpc.AgentBrokerRpc;
import org.limbo.flowjob.agent.core.rpc.AgentWorkerRpc;
import org.limbo.flowjob.agent.core.rpc.http.OkHttpAgentBrokerRpc;
import org.limbo.flowjob.agent.core.rpc.http.OkHttpAgentWorkerRpc;
import org.limbo.flowjob.agent.core.service.JobService;
import org.limbo.flowjob.agent.core.service.TaskService;
import org.limbo.flowjob.agent.core.worker.SingletonWorkerStatisticsRepo;
import org.limbo.flowjob.agent.core.worker.WorkerSelectorFactory;
import org.limbo.flowjob.agent.core.worker.WorkerStatisticsRepository;
import org.limbo.flowjob.agent.starter.SpringDelegatedAgent;
import org.limbo.flowjob.agent.starter.component.H2ConnectionFactory;
import org.limbo.flowjob.agent.starter.handler.HttpHandlerProcessor;
import org.limbo.flowjob.agent.starter.properties.AgentProperties;
import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.common.lb.BaseLBServer;
import org.limbo.flowjob.common.lb.BaseLBServerRepository;
import org.limbo.flowjob.common.lb.LBServerRepository;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;
import org.limbo.flowjob.common.rpc.EmbedHttpRpcServer;
import org.limbo.flowjob.common.rpc.EmbedRpcServer;
import org.limbo.flowjob.common.utils.NetUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-09-05
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "flowjob.agent", value = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AgentProperties.class)
public class FlowJobAgentAutoConfiguration {

    private final AgentProperties properties;

    private static final Integer DEFAULT_HTTP_SERVER_PORT = 9876;

    public FlowJobAgentAutoConfiguration(AgentProperties properties) {
        this.properties = properties;
    }


    /**
     * agent 实例，
     *
     * @param rpc broker rpc 通信模块
     */
    @Bean("fjaHttpScheduleAgent")
    public ScheduleAgent httpAgent(URL fjaAgentServerUrl, AgentResources resources, AgentBrokerRpc rpc, JobService jobService, TaskService taskService) {
        HttpHandlerProcessor httpHandlerProcessor = new HttpHandlerProcessor();
        EmbedRpcServer embedRpcServer = new EmbedHttpRpcServer(fjaAgentServerUrl.getPort(), httpHandlerProcessor);
        ScheduleAgent agent = new BaseScheduleAgent(fjaAgentServerUrl, resources, rpc, jobService, taskService, embedRpcServer);
        httpHandlerProcessor.setAgent(agent);
        httpHandlerProcessor.setTaskService(taskService);
        httpHandlerProcessor.setJobService(jobService);

        return new SpringDelegatedAgent(agent);
    }

    @Bean("fjaAgentServerUrl")
    public URL agentUrl() throws MalformedURLException {
        // 优先使用 SpringMVC 或 SpringWebflux 设置的端口号
        Integer port = properties.getPort() != null ? properties.getPort() : DEFAULT_HTTP_SERVER_PORT;
        // 优先使用指定的 host，如未指定则自动寻找本机 IP
        String host = properties.getHost();
        if (StringUtils.isEmpty(host)) {
            host = NetUtils.getLocalIp();
        }

        Assert.isTrue(port > 0, "Worker port must be a positive integer in range 1 ~ 65534");
        return new URL(properties.getProtocol().getValue(), host, port, "");
    }

    /**
     * 动态计算 Worker 资源
     */
    @Bean
    @ConditionalOnMissingBean(AgentResources.class)
    public AgentResources agentResource(JobService jobService) {
        return new BaseAgentResources(properties.getConcurrency(), properties.getQueueSize(), jobService);
    }

    @Bean("fjaJobService")
    public JobService jobService(JobRepository jobRepository, TaskRepository taskRepository, AgentBrokerRpc brokerRpc,
                                 TaskDispatcher taskDispatcher) {
        return new JobService(jobRepository, taskRepository, brokerRpc, taskDispatcher);
    }

    @Bean("fjaTaskService")
    public TaskService taskService(TaskRepository taskRepository, JobRepository jobRepository,
                                   TaskDispatcher taskDispatcher, AgentBrokerRpc brokerRpc) {
        return new TaskService(taskRepository, jobRepository, taskDispatcher, brokerRpc);
    }

    @Bean("fjaTaskRepository")
    public TaskRepository taskRepository(FlowjobConnectionFactory flowjobConnectionFactory) throws SQLException {
        TaskRepository taskRepository = new TaskRepository(flowjobConnectionFactory);
        // 先放这里了后面考虑生命周期
        if (properties.isInitTable()) {
            taskRepository.initTable();
        }
        return taskRepository;
    }

    @Bean("fjaConnectionFactory")
    @ConditionalOnMissingBean(FlowjobConnectionFactory.class)
    public FlowjobConnectionFactory flowjobConnectionFactory() {
        return new H2ConnectionFactory(properties.getDatasourceUrl(), properties.getDatasourceUsername(), properties.getDatasourcePassword());
    }


    @Bean("fjaJobRepository")
    public JobRepository jobRepository() {
        return new JobRepository();
    }

    @Bean("fjaTaskDispatcher")
    public TaskDispatcher taskDispatcher(AgentBrokerRpc brokerRpc, AgentWorkerRpc workerRpc,
                                         JobRepository jobRepository, TaskRepository taskRepository,
                                         WorkerSelectorFactory workerSelectorFactory, WorkerStatisticsRepository statisticsRepository) {
        return new TaskDispatcher(brokerRpc, workerRpc, jobRepository, taskRepository, workerSelectorFactory, statisticsRepository);
    }

    @Bean("fjaAgentWorkerRpc")
    @ConditionalOnMissingBean(AgentWorkerRpc.class)
    public AgentWorkerRpc workerRpc(URL fjaAgentServerUrl) {
        return new OkHttpAgentWorkerRpc(fjaAgentServerUrl);
    }



    /**
     * Broker 通信模块
     */
    @Bean("fjaAgentBrokerRpc")
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
    @Bean("fjaBrokerLoadBalanceRepo")
    @ConditionalOnMissingBean(name = "fjaBrokerLoadBalanceRepo")
    public LBServerRepository<BaseLBServer> brokerLoadBalanceRepo() {
        return new BaseLBServerRepository<>(brokerNodes());
    }


    /**
     * Broker 负载均衡策略
     */
    @Bean("fjaBrokerLoadBalanceStrategy")
    @ConditionalOnMissingBean(name = "fjaBrokerLoadBalanceStrategy")
    public LBStrategy<BaseLBServer> brokerLoadBalanceStrategy() {
        return new RoundRobinLBStrategy<>();
    }

    /**
     * 如果未声明 WorkerStatisticsRepository 类型的 Bean，则使用基于内存统计的单机模式
     */
    @Bean("fjaWorkerStatisticsRepository")
    @ConditionalOnMissingBean(WorkerStatisticsRepository.class)
    public WorkerStatisticsRepository workerStatisticsRepository() {
        return new SingletonWorkerStatisticsRepo();
    }

    /**
     * 用于生成 Worker 选择器，内部封装了 LB 算法的调用。
     */
    @Bean("fjaWorkerSelectorFactory")
    @ConditionalOnMissingBean(WorkerSelectorFactory.class)
    public WorkerSelectorFactory workerSelectorFactory(WorkerStatisticsRepository statisticsRepository) {
        WorkerSelectorFactory factory = new WorkerSelectorFactory();
        factory.setLbServerStatisticsProvider(statisticsRepository);
        return factory;
    }

}
