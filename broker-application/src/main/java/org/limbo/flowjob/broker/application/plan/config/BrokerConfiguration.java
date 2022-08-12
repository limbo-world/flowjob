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

import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.cluster.WorkerManagerImpl;
import org.limbo.flowjob.broker.core.dispatcher.strategies.RoundRobinWorkerSelector;
import org.limbo.flowjob.broker.core.plan.job.context.TaskCreatorFactory;
import org.limbo.flowjob.broker.core.schedule.calculator.SimpleScheduleCalculatorFactory;
import org.limbo.flowjob.broker.core.schedule.scheduler.HashedWheelTimerScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@EnableConfigurationProperties({BrokerProperties.class})
public class BrokerConfiguration {

    @Value("${server.port}")
    private int port;

    @Autowired
    private BrokerProperties brokerProperties;


    /**
     * worker 管理，持久化等
     */
    @Bean
    public WorkerManager workerManager(WorkerRepository workerRepository) {
        return new WorkerManagerImpl(workerRepository);
    }


    /**
     * 调度时间计算器
     */
    @Bean
    public SimpleScheduleCalculatorFactory scheduleCalculatorFactory() {
        return new SimpleScheduleCalculatorFactory();
    }


    /**
     * Worker负载均衡：轮询
     */
    @Bean
    public RoundRobinWorkerSelector roundRobinWorkerSelector() {
        return new RoundRobinWorkerSelector();
    }


    /**
     * 任务生成策略工厂
     */
    @Bean
    public TaskCreatorFactory taskCreateStrategyFactory(WorkerManager workerManager) {
        return new TaskCreatorFactory(workerManager);
    }

    /**
     * 调度器
     */
    @Bean
    public Scheduler scheduler() {
        return new HashedWheelTimerScheduler(new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() * 4,
                Runtime.getRuntime().availableProcessors() * 8,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(256),
                new ThreadPoolExecutor.CallerRunsPolicy()));
    }

}
