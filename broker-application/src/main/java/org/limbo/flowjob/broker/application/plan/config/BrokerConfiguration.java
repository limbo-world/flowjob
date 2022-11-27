/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.application.plan.config;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.application.plan.component.JobStatusCheckTask;
import org.limbo.flowjob.broker.application.plan.component.PlanScheduleTask;
import org.limbo.flowjob.broker.application.plan.component.TaskStatusCheckTask;
import org.limbo.flowjob.broker.application.plan.support.BrokerStarter;
import org.limbo.flowjob.broker.application.plan.support.NodeMangerImpl;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.cluster.NodeRegistry;
import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.cluster.WorkerManagerImpl;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.job.JobFactory;
import org.limbo.flowjob.broker.core.domain.plan.PlanFactory;
import org.limbo.flowjob.broker.core.domain.task.TaskFactory;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

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

    /**
     * worker 管理，持久化等
     */
    @Bean
    public WorkerManager workerManager(WorkerRepository workerRepository) {
        return new WorkerManagerImpl(workerRepository);
    }

    @Bean
    public TaskFactory taskFactory(WorkerManager workerManager, IDGenerator idGenerator) {
        return new TaskFactory(workerManager, idGenerator);
    }

    @Bean
    public JobFactory jobFactory(IDGenerator idGenerator) {
        return new JobFactory(idGenerator);
    }

    @Bean
    public PlanFactory planFactory(IDGenerator idGenerator) {
        return new PlanFactory(idGenerator);
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
    public MetaTask planScheduleMetaTask(BrokerConfig config, NodeManger nodeManger, PlanFactory planFactory) {
        return new PlanScheduleTask(Duration.ofMillis(brokerProperties.getRebalanceInterval()), config, nodeManger, planFactory);
    }


    /**
     * 元任务：Task 状态检查，判断任务是否失败
     */
    @Bean
    public MetaTask taskStatusCheckTask() {
        return new TaskStatusCheckTask(Duration.ofMillis(brokerProperties.getStatusCheckInterval()));
    }


    /**
     * 元任务：Job 状态检查
     */
    @Bean
    public MetaTask jobStatusCheckTask() {
        return new JobStatusCheckTask(Duration.ofMillis(brokerProperties.getStatusCheckInterval()));
    }

}
