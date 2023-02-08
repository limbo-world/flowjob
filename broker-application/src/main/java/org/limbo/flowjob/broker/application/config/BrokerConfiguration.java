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
import org.limbo.flowjob.broker.application.component.BrokerStarter;
import org.limbo.flowjob.broker.application.component.PlanLoadTask;
import org.limbo.flowjob.broker.application.component.TaskStatusCheckTask;
import org.limbo.flowjob.broker.application.support.NodeMangerImpl;
import org.limbo.flowjob.broker.cluster.DBBrokerRegistry;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.cluster.NodeRegistry;
import org.limbo.flowjob.broker.core.dispatch.TaskDispatcher;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.task.TaskFactory;
import org.limbo.flowjob.broker.core.domain.task.TaskManager;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.strategy.ITaskResultStrategy;
import org.limbo.flowjob.broker.core.statistics.WorkerStatisticsRepository;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.limbo.flowjob.broker.dao.domain.SingletonWorkerStatisticsRepo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({BrokerProperties.class})
public class BrokerConfiguration {

    @Setter(onMethod_ = @Inject)
    private BrokerProperties brokerProperties;

    @Setter(onMethod_ = @Inject)
    private NodeRegistry brokerRegistry;

    /**
     * worker 管理，持久化等
     *
     * @param metaTasks 下面定义的MetaTask的bean
     */
    @Bean
    @ConditionalOnProperty(prefix = "flowjob.broker", value = "enabled", havingValue = "true", matchIfMissing = true)
    public Broker brokerNode(NodeManger nodeManger, MetaTaskScheduler metaTaskScheduler, List<MetaTask> metaTasks) {
        return new BrokerStarter(brokerProperties, brokerRegistry, nodeManger, metaTaskScheduler, metaTasks);
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
    public WorkerStatisticsRepository WorkerStatisticsRepository() {
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


    /**
     * 用于分发任务
     */
    @Bean
    public TaskDispatcher taskDispatcher(WorkerRepository workerRepository, WorkerSelectorFactory factory, WorkerStatisticsRepository statisticsRepository) {
        return new TaskDispatcher(workerRepository, factory, statisticsRepository);
    }

    /**
     * 元任务调度器
     */
    @Bean
    public MetaTaskScheduler metaTaskScheduler() {
        return new MetaTaskScheduler();
    }


    /**
     * 元任务：Plan 加载与调度
     */
    @Bean
    public MetaTask planScheduleMetaTask(BrokerConfig config, NodeManger nodeManger, MetaTaskScheduler metaTaskScheduler) {
        return new PlanLoadTask(Duration.ofMillis(brokerProperties.getRebalanceInterval()), config, nodeManger, metaTaskScheduler);
    }


    /**
     * 元任务：Task 状态检查，判断任务是否失败
     */
    @Bean
    public MetaTask taskStatusCheckTask(BrokerConfig config,
                                        NodeManger nodeManger,
                                        MetaTaskScheduler metaTaskScheduler,
                                        WorkerRepository workerRepository,
                                        ITaskResultStrategy scheduleStrategy) {
        return new TaskStatusCheckTask(
                Duration.ofMillis(brokerProperties.getStatusCheckInterval()),
                config,
                nodeManger,
                metaTaskScheduler,
                workerRepository,
                scheduleStrategy
        );
    }

    @Bean
    public ExecutorService planSchedulePool() {
        return new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() * 4,
                Runtime.getRuntime().availableProcessors() * 4,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(256),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public ExecutorService taskSchedulePool() {
        return new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() * 8,
                Runtime.getRuntime().availableProcessors() * 8,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1024),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public DBBrokerRegistry brokerRegistry(BrokerConfig config) {
        return new DBBrokerRegistry(1000, config.getHeartbeatInterval(), config.getHeartbeatTimeout());
    }

}
