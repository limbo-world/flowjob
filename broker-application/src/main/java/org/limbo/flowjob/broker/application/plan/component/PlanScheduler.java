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
import org.limbo.flowjob.broker.application.plan.service.ScheduleService;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.schedule.scheduler.HashedWheelTimerScheduler;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
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
    private ScheduleService scheduleService;

    /**
     * 调度线程池
     */
    @Setter(onMethod_ = {@Inject, @Named("planSchedulePool")})
    private ExecutorService schedulePool;

    @Override
    protected void doSchedule(PlanInstance planInstance) {
        if (log.isDebugEnabled()) {
            log.info("[PlanScheduler] submit planInstance: {}", planInstance);
        }
        // 执行调度逻辑
        schedulePool.submit(() -> {
            try {
                scheduleService.schedulePlanInstance(planInstance);
            } catch (Exception e) {
                log.error("PlanScheduler schedule error planInstance:{}", planInstance, e);
            } finally {
                // 完成后移除
                unschedule(planInstance.scheduleId());
            }
        });
    }

}
