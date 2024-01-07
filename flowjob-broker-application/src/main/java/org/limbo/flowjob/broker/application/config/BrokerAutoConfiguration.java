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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.application.component.BrokerStarter;
import org.limbo.flowjob.broker.application.component.DBBrokerRegistry;
import org.limbo.flowjob.broker.core.agent.AgentRegistry;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.cluster.NodeRegistry;
import org.limbo.flowjob.broker.core.meta.IDGenerator;
import org.limbo.flowjob.broker.core.meta.info.PlanRepository;
import org.limbo.flowjob.broker.core.meta.instance.DelayInstanceRepository;
import org.limbo.flowjob.broker.core.meta.instance.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.meta.processor.DelayInstanceProcessor;
import org.limbo.flowjob.broker.core.meta.processor.PlanInstanceProcessor;
import org.limbo.flowjob.broker.core.meta.task.JobExecuteCheckTask;
import org.limbo.flowjob.broker.core.meta.task.JobScheduleCheckTask;
import org.limbo.flowjob.broker.core.meta.task.PlanLoadTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.selector.SingletonWorkerStatisticsRepo;
import org.limbo.flowjob.broker.core.schedule.selector.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.schedule.selector.WorkerStatisticsRepository;
import org.limbo.flowjob.broker.core.service.TransactionService;
import org.limbo.flowjob.broker.core.worker.WorkerDomainService;
import org.limbo.flowjob.broker.core.worker.WorkerRegistry;
import org.limbo.flowjob.broker.dao.repositories.BrokerEntityRepo;
import org.limbo.flowjob.common.utils.NetUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({BrokerProperties.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.ANY)
@ConditionalOnProperty(prefix = "flowjob.broker", value = "enabled", havingValue = "true", matchIfMissing = true)
public class BrokerAutoConfiguration {

    @Setter(onMethod_ = @Inject)
    private BrokerProperties brokerProperties;

    @Setter(onMethod_ = @Inject)
    private NodeRegistry brokerRegistry;

    @Setter(onMethod_ = @Value("${server.port:8080}"))
    private Integer httpServerPort;

    /**
     * worker 管理，持久化等
     */
    @Bean
    public Broker broker(URL brokerUrl, NodeManger nodeManger, MetaTaskScheduler metaTaskScheduler, List<MetaTask> metaTasks) {
        return new BrokerStarter(brokerProperties.getName(), brokerUrl, brokerRegistry, nodeManger, metaTaskScheduler, metaTasks);
    }

    @Bean
    public NodeManger brokerManger(PlanRepository planRepository, JobInstanceRepository jobInstanceRepository) {
        return new NodeManger(planRepository, jobInstanceRepository);
    }

    @Bean
    public URL brokerUrl() throws MalformedURLException {
        Integer port = brokerProperties.getPort() != null ? brokerProperties.getPort() : httpServerPort;
        // 优先使用指定的 host，如未指定则自动寻找本机 IP
        String host = brokerProperties.getHost();
        if (StringUtils.isEmpty(host)) {
            host = NetUtils.getLocalIp();
        }
        Assert.isTrue(port > 0, "port must be a positive integer in range 1 ~ 65534");
        return new URL(brokerProperties.getProtocol().getValue(), host, port, "");
    }

    /**
     * 元任务调度器 目前支持秒级任务
     */
    @Bean
    public MetaTaskScheduler metaTaskScheduler() {
        return new MetaTaskScheduler(100L, TimeUnit.MILLISECONDS);
    }

    @Bean
    public DBBrokerRegistry brokerRegistry(BrokerConfig config, BrokerEntityRepo brokerEntityRepo, IDGenerator idGenerator) {
        return new DBBrokerRegistry(config.getHeartbeatInterval(), config.getHeartbeatTimeout(), brokerEntityRepo, idGenerator);
    }

    /**
     * 如果未声明 WorkerStatisticsRepository 类型的 Bean，则使用基于内存统计的单机模式
     */
    @Bean("fjWorkerStatisticsRepository")
    @ConditionalOnMissingBean(WorkerStatisticsRepository.class)
    public WorkerStatisticsRepository workerStatisticsRepository() {
        return new SingletonWorkerStatisticsRepo();
    }

    /**
     * 用于生成 Worker 选择器，内部封装了 LB 算法的调用。
     */
    @Bean("fjWorkerSelectorFactory")
    @ConditionalOnMissingBean(WorkerSelectorFactory.class)
    public WorkerSelectorFactory workerSelectorFactory(WorkerStatisticsRepository statisticsRepository) {
        WorkerSelectorFactory factory = new WorkerSelectorFactory();
        factory.setLbServerStatisticsProvider(statisticsRepository);
        return factory;
    }

    @Bean
    public PlanLoadTask planLoadTask(MetaTaskScheduler scheduler,
                                     PlanRepository planRepository,
                                     PlanInstanceProcessor processor,
                                     @Lazy Broker broker,
                                     NodeManger nodeManger) {
        return new PlanLoadTask(scheduler, planRepository, processor, broker, nodeManger);
    }

    @Bean
    public JobExecuteCheckTask jobExecuteCheckTask(MetaTaskScheduler metaTaskScheduler,
                                                   JobInstanceRepository jobInstanceRepository,
                                                   @Lazy Broker broker,
                                                   NodeManger nodeManger,
                                                   PlanInstanceProcessor processor) {
        return new JobExecuteCheckTask(metaTaskScheduler, jobInstanceRepository, broker, nodeManger, processor);
    }

    @Bean
    public JobScheduleCheckTask jobScheduleCheckTask(MetaTaskScheduler scheduler,
                                                     @Lazy Broker broker,
                                                     NodeManger nodeManger,
                                                     AgentRegistry agentRegistry,
                                                     JobInstanceRepository jobInstanceRepository) {
        return new JobScheduleCheckTask(scheduler, broker, nodeManger, agentRegistry, jobInstanceRepository);
    }

    @Bean
    public WorkerDomainService workerDomainService(WorkerRegistry workerRegistry,
                                                   WorkerSelectorFactory workerSelectorFactory,
                                                   WorkerStatisticsRepository workerStatisticsRepository) {
        return new WorkerDomainService(workerRegistry, workerSelectorFactory, workerStatisticsRepository);
    }

    @Bean
    public PlanInstanceProcessor planInstanceProcessor(MetaTaskScheduler metaTaskScheduler,
                                                       IDGenerator idGenerator,
                                                       NodeManger nodeManger,
                                                       AgentRegistry agentRegistry,
                                                       PlanRepository planRepository,
                                                       TransactionService transactionService,
                                                       PlanInstanceRepository planInstanceRepository,
                                                       JobInstanceRepository jobInstanceRepository) {
        return new PlanInstanceProcessor(metaTaskScheduler, idGenerator, nodeManger, agentRegistry, planRepository, transactionService, planInstanceRepository, jobInstanceRepository);
    }

    @Bean
    public DelayInstanceProcessor delayInstanceProcessor(MetaTaskScheduler metaTaskScheduler,
                                                         IDGenerator idGenerator,
                                                         NodeManger nodeManger,
                                                         AgentRegistry agentRegistry,
                                                         TransactionService transactionService,
                                                         DelayInstanceRepository delayInstanceRepository,
                                                         JobInstanceRepository jobInstanceRepository) {
        return new DelayInstanceProcessor(metaTaskScheduler, idGenerator, nodeManger, agentRegistry, transactionService, delayInstanceRepository, jobInstanceRepository);
    }

}
