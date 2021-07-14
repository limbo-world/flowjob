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

package org.limbo.flowjob.tracker.infrastructure.config;

import org.limbo.flowjob.tracker.core.dispatcher.JobDispatchServiceFactory;
import org.limbo.flowjob.tracker.core.dispatcher.SimpleJobDispatchServiceFactory;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.tracker.core.schedule.executor.Executor;
import org.limbo.flowjob.tracker.core.schedule.executor.JobExecutor;
import org.limbo.flowjob.tracker.core.schedule.scheduler.HashedWheelTimerScheduler;
import org.limbo.flowjob.tracker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.limbo.flowjob.tracker.core.tracker.LeaderJobTracker;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
public class JobTrackerConfiguration {


    /**
     * JobTracker
     */
    @Bean
    @ConditionalOnMissingBean(LeaderJobTracker.class)
    public LeaderJobTracker jobTracker(WorkerRepository workerRepository) {
        return new LeaderJobTracker(workerRepository);
    }


    /**
     * 作业调度器
     */
    @Bean
    @ConditionalOnMissingBean(Scheduler.class)
    public Scheduler<JobContext> jobScheduler(Executor<JobContext> executor) {
        return new HashedWheelTimerScheduler<>(executor);
    }


    /**
     * 作业执行器
     */
    @Bean
    @ConditionalOnMissingBean(Executor.class)
    public Executor<JobContext> jobExecutor(JobTracker jobTracker,
                                            JobDispatchServiceFactory dispatchServiceFactory) {
        return new JobExecutor(jobTracker, dispatchServiceFactory);
    }


    /**
     * 作业调度时间计算器工厂，根据作业调度类型来生产调度时间计算器。
     */
    @Bean
    @ConditionalOnMissingBean(ScheduleCalculatorFactory.class)
    public ScheduleCalculatorFactory jobScheduleCalculatorFactory() {
        return new ScheduleCalculatorFactory();
    }


    /**
     * 作业分发服务工厂，根据作业分发类型，生产对应的分发服务。
     */
    @Bean
    @ConditionalOnMissingBean(JobDispatchServiceFactory.class)
    public JobDispatchServiceFactory jobDispatchServiceFactory(PlanRepository planRepository) {
        return new SimpleJobDispatchServiceFactory(planRepository);
    }

}
