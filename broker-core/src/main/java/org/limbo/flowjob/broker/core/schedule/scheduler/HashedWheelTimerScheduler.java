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

package org.limbo.flowjob.broker.core.schedule.scheduler;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.schedule.Schedulable;
import org.limbo.flowjob.broker.core.utils.TimeUtil;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于Netty时间轮算法的作业执行器。一个作业申请执行后，会计算下次执行的间隔，并注册到时间轮上。
 * 当时间轮触发作业执行时，将进入作业下发流程，并将生成的实例分发给下游。
 *
 * @author Brozen
 * @since 2021-05-18
 */
@Slf4j
public class HashedWheelTimerScheduler implements Scheduler {

    /**
     * 依赖netty的时间轮算法进行作业调度
     */
    private final Timer timer;

    /**
     * 所有正在被调度中的对象
     */
    private final Map<String, Schedulable> scheduling;

    /**
     * 调度线程池
     */
    private final ExecutorService schedulePool;

    /**
     * 使用指定执行器构造一个调度器，该调度器基于哈希时间轮算法。
     */
    public HashedWheelTimerScheduler() {
        this.timer = new HashedWheelTimer(NamedThreadFactory.newInstance(this.getClass().getSimpleName() + "-timer-"));
        this.scheduling = new ConcurrentHashMap<>();
        this.schedulePool = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() * 2,
                Runtime.getRuntime().availableProcessors() * 4,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(64),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * {@inheritDoc}
     *
     * @param schedulable 待调度的对象
     */
    @Override
    public void schedule(Schedulable schedulable) {
        String id = schedulable.scheduleId();
        if (scheduling.containsKey(id)) {
            scheduling.put(id, schedulable);

            // 计算延迟时间
            long delay = Duration.between(TimeUtil.nowLocalDateTime(), schedulable.triggerAt()).toMillis();
            delay = delay < 0 ? 0 : delay;

            // 在timer上调度作业执行
            this.timer.newTimeout(timeout -> {
                try {
                    // 已经取消调度了，则不再重新调度作业
                    if (!scheduling.containsKey(id)) {
                        return;
                    }

                    // 执行调度逻辑
                    schedulePool.submit(schedulable::schedule);
                } catch (Exception e) {
                    log.error("[HashedWheelTimerScheduler] schedule fail id:{}", id, e);
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @param id 待调度的对象的id
     */
    @Override
    public void unschedule(String id) {
        scheduling.remove(id);
    }


    /**
     * {@inheritDoc}
     *
     * @param id 调度的对象 id
     * @return 是否调度中
     */
    @Override
    public boolean isScheduling(String id) {
        return scheduling.containsKey(id);
    }

}
