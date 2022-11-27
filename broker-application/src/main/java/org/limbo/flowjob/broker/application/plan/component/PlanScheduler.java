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
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.application.plan.service.PlanService;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.schedule.scheduler.HashedWheelTimerScheduler;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 用于调度 PlanInstance
 *
 * @author Devil
 * @since 2022/8/18
 */
@Slf4j
@Component
public class PlanScheduler extends HashedWheelTimerScheduler<PlanInstance> {

    @Setter(onMethod_ = @Inject)
    private PlanService planService;

    @Setter(onMethod_ = @Inject)
    private JobScheduler jobScheduler;

    /**
     * 调度线程池
     */
    private final ExecutorService schedulePool = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 4,
            Runtime.getRuntime().availableProcessors() * 4,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(256),
            new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    protected void doSchedule(PlanInstance planInstance) {
        if (log.isDebugEnabled()) {
            log.debug("[PlanScheduler] submit planInstance: {}", planInstance);
        }
        // 执行调度逻辑
        schedulePool.submit(() -> {
            try {
                // 保存数据
                planInstance.trigger();
                List<JobInstance> rootJobs = planService.rootJobs(planInstance);
                planService.saveScheduleInfo(planInstance, rootJobs);

                // 执行调度逻辑
                for (JobInstance instance : rootJobs) {
                    jobScheduler.schedule(instance);
                }
            } catch (Exception e) {
                log.error("PlanScheduler schedule error planInstance:{}", planInstance, e);
            } finally {
                // 完成后移除
                unschedule(planInstance.scheduleId());
            }
        });
    }

}
