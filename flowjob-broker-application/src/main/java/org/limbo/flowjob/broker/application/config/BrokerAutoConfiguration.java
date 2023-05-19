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
import org.limbo.flowjob.broker.application.support.NodeMangerImpl;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.cluster.NodeRegistry;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.dispatch.TaskDispatcher;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.task.TaskFactory;
import org.limbo.flowjob.broker.core.domain.task.TaskManager;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.statistics.WorkerStatisticsRepository;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.limbo.flowjob.broker.dao.domain.SingletonWorkerStatisticsRepo;
import org.limbo.flowjob.broker.dao.repositories.BrokerEntityRepo;
import org.limbo.flowjob.common.utils.NetUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public Broker broker(NodeManger nodeManger, MetaTaskScheduler metaTaskScheduler, List<MetaTask> metaTasks) throws MalformedURLException {
        Integer port = brokerProperties.getPort() != null ? brokerProperties.getPort() : httpServerPort;
        // 优先使用指定的 host，如未指定则自动寻找本机 IP
        String host = brokerProperties.getHost();
        if (StringUtils.isEmpty(host)) {
            host = NetUtils.getLocalIp();
        }
        Assert.isTrue(port > 0, "port must be a positive integer in range 1 ~ 65534");
        URL baseUrl = new URL(brokerProperties.getProtocol().getValue(), host, port, "");
        return new BrokerStarter(brokerProperties.getName(), baseUrl, brokerRegistry, nodeManger, metaTaskScheduler, metaTasks);
    }

    @Bean
    public NodeManger brokerManger() {
        return new NodeMangerImpl();
    }

    @Bean
    public TaskFactory taskFactory(WorkerRepository workerRepository, TaskManager taskManager, IDGenerator idGenerator) {
        return new TaskFactory(workerRepository, taskManager, idGenerator);
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
    public DispatchOption.WorkerSelectorFactory workerSelectorFactory(WorkerStatisticsRepository statisticsRepository) {
        DispatchOption.WorkerSelectorFactory factory = new DispatchOption.WorkerSelectorFactory();
        factory.setLbServerStatisticsProvider(statisticsRepository);
        return factory;
    }


    /**
     * 用于分发任务
     */
    @Bean
    public TaskDispatcher taskDispatcher(WorkerRepository workerRepository, DispatchOption.WorkerSelectorFactory factory, WorkerStatisticsRepository statisticsRepository) {
        return new TaskDispatcher(workerRepository, factory, statisticsRepository);
    }

    /**
     * 元任务调度器 目前支持秒级任务
     */
    @Bean
    public MetaTaskScheduler metaTaskScheduler() {
        return new MetaTaskScheduler(1000L, TimeUnit.MILLISECONDS);
    }

    @Bean
    public DBBrokerRegistry brokerRegistry(BrokerConfig config, BrokerEntityRepo brokerEntityRepo, IDGenerator idGenerator) {
        return new DBBrokerRegistry(1000, config.getHeartbeatInterval(), config.getHeartbeatTimeout(), brokerEntityRepo, idGenerator);
    }

}
