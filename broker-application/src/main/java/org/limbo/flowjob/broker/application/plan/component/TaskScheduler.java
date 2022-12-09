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
import org.limbo.flowjob.broker.application.plan.manager.TaskScheduleManager;
import org.limbo.flowjob.broker.core.domain.task.Task;
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
@Slf4j
@Component
public class TaskScheduler extends HashedWheelTimerScheduler<Task> {

    @Setter(onMethod_ = @Inject)
    private TaskScheduleManager taskScheduleManager;

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

    @Override
    protected void doSchedule(Task task) {
        if (log.isDebugEnabled()) {
            log.debug("[TaskScheduler] submit task: {}", task);
        }
        // 执行调度逻辑
        schedulePool.submit(() -> {
            try {
                taskScheduleManager.dispatch(task);
            } catch (Exception e) {
                log.error("TaskScheduler schedule error task:{}", task, e);
            } finally {
                // 完成后移除
                unschedule(task.scheduleId());
            }
        });
    }

}
