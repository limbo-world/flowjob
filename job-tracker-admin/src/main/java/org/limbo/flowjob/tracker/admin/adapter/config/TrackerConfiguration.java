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

package org.limbo.flowjob.tracker.admin.adapter.config;

import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.TrackerModes;
import org.limbo.flowjob.tracker.core.dispatcher.DispatchLauncher;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.job.context.JobRecordRepository;
import org.limbo.flowjob.tracker.core.plan.PlanBuilderFactory;
import org.limbo.flowjob.tracker.core.plan.PlanExecutor;
import org.limbo.flowjob.tracker.core.plan.PlanInstanceRepository;
import org.limbo.flowjob.tracker.core.plan.PlanRecordRepository;
import org.limbo.flowjob.tracker.core.raft.ElectionNodeOptions;
import org.limbo.flowjob.tracker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.tracker.core.schedule.scheduler.HashedWheelTimerScheduler;
import org.limbo.flowjob.tracker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.tracker.core.storage.MemoryStorage;
import org.limbo.flowjob.tracker.core.storage.Storage;
import org.limbo.flowjob.tracker.core.tracker.JobTrackerFactory;
import org.limbo.flowjob.tracker.core.tracker.TrackerNode;
import org.limbo.flowjob.tracker.core.tracker.WorkerManager;
import org.limbo.flowjob.tracker.core.tracker.WorkerManagerImpl;
import org.limbo.flowjob.tracker.core.tracker.election.ElectionTrackerNode;
import org.limbo.flowjob.tracker.core.tracker.single.SingleTrackerNode;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@ComponentScan({
        "org.limbo.flowjob.tracker.infrastructure.plan",
        "org.limbo.flowjob.tracker.infrastructure.job",
        "org.limbo.flowjob.tracker.infrastructure.worker",
})
@EnableConfigurationProperties({TrackerProperties.class})
public class TrackerConfiguration {

    @Value("${server.port}")
    private int port;

    @Autowired
    private TrackerProperties trackerProperties;

    @Bean
    @ConditionalOnMissingBean(TrackerNode.class)
    public TrackerNode trackerNode(WorkerManager workerManager,
                                   JobTrackerFactory jobTrackerFactory) {

        TrackerModes mode = TrackerModes.parse(trackerProperties.getMode());
        switch (mode) {
            case SINGLE:
                return new SingleTrackerNode(trackerProperties.getHost(), port, jobTrackerFactory, workerManager);

            case ELECTION: {
                // raft 选举参数
                ElectionNodeOptions electionNodeOptions = new ElectionNodeOptions();
                electionNodeOptions.setDataPath(trackerProperties.getDataPath());
                electionNodeOptions.setGroupId(StringUtils.isBlank(trackerProperties.getGroupId()) ? "flowjob" :
                        trackerProperties.getDataPath());
                electionNodeOptions.setServerAddress(trackerProperties.getServerAddress());
                electionNodeOptions.setServerAddressList(trackerProperties.getServerAddressList());

                return new ElectionTrackerNode(port, electionNodeOptions, jobTrackerFactory, workerManager);
            }

            case CLUSTER:
                throw new UnsupportedOperationException("cluster mode is not supported now.");

            default:
                throw new IllegalArgumentException("flowjob.tracker.mode only can be null or election/cluster");


        }

    }


    /**
     * 主从模式下，tracker 节点工厂
     */
    @Bean
    public JobTrackerFactory jobTrackerFactory(Scheduler scheduler,
                                               Storage storage,
                                               DispatchLauncher dispatchLauncher) {
        return new JobTrackerFactory(storage, scheduler, dispatchLauncher);
    }


    /**
     * 任务调度器
     */
    @Bean
    public Scheduler scheduler() {
        return new HashedWheelTimerScheduler();
    }


    /**
     * 异步进行任务下发的launcher
     */
    @Bean
    public DispatchLauncher jobDispatchLauncher(Storage storage,
                                                WorkerManager workerManager,
                                                PlanRecordRepository planRecordRepository,
                                                PlanInstanceRepository planInstanceRepository,
                                                JobRecordRepository jobRecordRepository,
                                                JobInstanceRepository jobInstanceRepository) {
        return new DispatchLauncher(workerManager, storage, planRecordRepository, planInstanceRepository, jobRecordRepository, jobInstanceRepository);
    }


    /**
     * worker 管理，持久化等
     */
    @Bean
    public WorkerManager workerManager(WorkerRepository workerRepository) {
        return new WorkerManagerImpl(workerRepository);
    }


    /**
     * job 存储
     */
    @Bean
    @ConditionalOnMissingBean(Storage.class)
    public Storage jobStorage() {
        return new MemoryStorage();
    }

    /**
     * 计划工厂
     */
    @Bean
    @ConditionalOnMissingBean(PlanBuilderFactory.class)
    public PlanBuilderFactory planFactory(Storage storage) {
        return new PlanBuilderFactory(
                new ScheduleCalculatorFactory(),
                new PlanExecutor(storage)
        );
    }

}
