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

package org.limbo.flowjob.tracker.core.schedule.scheduler;

import io.netty.util.HashedWheelTimer;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.SchedulableContext;
import org.limbo.flowjob.tracker.core.schedule.executor.Executor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于Netty时间轮算法的作业执行器。一个作业申请执行后，会计算下次执行的间隔，并注册到时间轮上。
 * 当时间轮触发作业执行时，将进入作业下发流程，并将生成的JobContext分发给下游。
 *
 * @author Brozen
 * @since 2021-05-18
 */
public class HashedWheelTimerScheduler<T extends SchedulableContext> implements Scheduler<T> {

    private static final Object PLACEHOLDER = new Object();

    /**
     * 依赖netty的时间轮算法进行作业调度
     */
    private HashedWheelTimer timer;

    /**
     * 所有正在被调度中的对象
     */
    private Map<String, Object> scheduling;

    /**
     * 调度对象执行器
     */
    private Executor<T> executor;

    /**
     * 使用指定执行器构造一个调度器，该调度器基于哈希时间轮算法。
     */
    public HashedWheelTimerScheduler(Executor<T> executor) {
        this.executor = executor;

        this.timer = new HashedWheelTimer(NamedThreadFactory.newInstance(this.getClass().getSimpleName() + "-timer-"));
        this.scheduling = new ConcurrentHashMap<>();
    }


    /**
     * {@inheritDoc}
     * @param schedulable 待调度的对象
     */
    @Override
    public void schedule(Schedulable<T> schedulable) {
        // 如果job不会被触发，则无需加入调度
        long triggerAt = schedulable.nextTriggerAt();
        if (triggerAt <= 0) {
            return;
        }

        // 防止重复调度
        scheduling.computeIfAbsent(schedulable.getId(), jobId -> {
            doScheduleJob(schedulable, triggerAt);
            return PLACEHOLDER;
        });
    }

    /**
     * 检测是否需要重新调度
     * @param schedulable 待调度的对象
     */
    protected void rescheduleJob(Schedulable<T> schedulable) {
        // 如果job不会被触发，则无需继续调度
        long triggerAt = schedulable.nextTriggerAt();
        if (triggerAt <= 0) {
            scheduling.remove(schedulable.getId());
            return;
        }

        doScheduleJob(schedulable, triggerAt);
    }

    /**
     * 执行调度，根据triggerAt计算执行delay，并注册到timer上执行。
     * @param schedulable 待调度对象
     * @param triggerAt 下次被调度执行的时间戳
     */
    private void doScheduleJob(Schedulable<T> schedulable, long triggerAt) {
        // 在timer上调度作业执行
        long delay = triggerAt - System.currentTimeMillis();
        this.timer.newTimeout(timeout -> {

            // 已经取消调度了，则不再重新调度作业
            if (!scheduling.containsKey(schedulable.getId())) {
                return;
            }

            // 生成新的上下文，并交给执行器执行
            T context = schedulable.getContext();
            executor.execute(context);

            // 检测是否需要重新调度
            this.rescheduleJob(schedulable);

        }, delay, TimeUnit.MILLISECONDS);
    }


    /**
     * {@inheritDoc}
     * @param schedulable 待调度的对象
     */
    @Override
    public void unschedule(Schedulable<T> schedulable) {
        scheduling.remove(schedulable.getId());
    }

}
