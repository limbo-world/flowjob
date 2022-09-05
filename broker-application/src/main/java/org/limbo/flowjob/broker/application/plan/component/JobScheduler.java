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

package org.limbo.flowjob.broker.application.plan.component;

import lombok.Setter;
import org.limbo.flowjob.broker.application.plan.service.JobService;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.schedule.scheduler.HashedWheelTimerScheduler;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Devil
 * @since 2022/8/18
 */
@Component
public class JobScheduler extends HashedWheelTimerScheduler<JobInstance> {

    @Setter(onMethod_ = @Inject)
    private JobService jobService;

    /**
     * 调度线程池
     */
    private final ExecutorService schedulePool = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 8,
            Runtime.getRuntime().availableProcessors() * 8,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1024),
            new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 由于是延迟触发的 调度前 自行保存 jobinstance信息
     */
    @Override
    protected void doSchedule(JobInstance jobInstance) {
        // 执行调度逻辑
        schedulePool.submit(() -> {
            jobService.dispatch(jobInstance);

            // 完成后移除
            unschedule(jobInstance.scheduleId());
        });
    }

}
