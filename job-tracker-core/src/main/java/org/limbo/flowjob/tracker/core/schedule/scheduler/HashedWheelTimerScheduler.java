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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于Netty时间轮算法的作业执行器。一个作业申请执行后，会计算下次执行的间隔，并注册到时间轮上。
 * 当时间轮触发作业执行时，将进入作业下发流程，并将生成的实例分发给下游。
 *
 * @author Brozen
 * @since 2021-05-18
 */
public class HashedWheelTimerScheduler implements Scheduler {

    /**
     * 依赖netty的时间轮算法进行作业调度
     */
    private final HashedWheelTimer timer;

    /**
     * 所有正在被调度中的对象
     */
    private final Map<String, Schedulable> scheduling;

    /**
     * 使用指定执行器构造一个调度器，该调度器基于哈希时间轮算法。
     */
    public HashedWheelTimerScheduler() {
        this.timer = new HashedWheelTimer(NamedThreadFactory.newInstance(this.getClass().getSimpleName() + "-timer-"));
        this.scheduling = new ConcurrentHashMap<>();
    }


    /**
     * {@inheritDoc}
     * @param schedulable 待调度的对象
     */
    @Override
    public void schedule(Schedulable schedulable) {
        // 如果不会被触发，则无需加入调度
        long triggerAt = schedulable.nextTriggerAt();
        if (triggerAt <= 0) {
            return;
        }

        // 如果值本来不存在 启动调度
        if (scheduling.put(schedulable.getId(), schedulable) == null) {
            doSchedule(schedulable, triggerAt);
        }
    }


    /**
     * 检测是否需要重新调度
     * @param schedulable 待调度的对象
     */
    protected void reschedule(Schedulable schedulable) {
        // 如果不会被触发，则无需继续调度
        long triggerAt = schedulable.nextTriggerAt();
        if (triggerAt <= 0) {
            scheduling.remove(schedulable.getId());
            return;
        }

        doSchedule(schedulable, triggerAt);
    }


    /**
     * 执行调度，根据triggerAt计算执行delay，并注册到timer上执行。
     * @param schedulable 待调度对象
     * @param triggerAt 下次被调度执行的时间戳
     */
    private void doSchedule(Schedulable schedulable, long triggerAt) {
        // 在timer上调度作业执行
        long delay = triggerAt - System.currentTimeMillis(); // todo 这个delay计算是不是有点问题
        this.timer.newTimeout(timeout -> {

            // 已经取消调度了，则不再重新调度作业
            if (!scheduling.containsKey(schedulable.getId())) {
                return;
            }

            // 执行调度逻辑 todo 可以交由线程池？增加下发速度
            schedulable.schedule();

            // 检测是否需要重新调度 todo 不应该直接就触发
            this.reschedule(schedulable);

        }, delay, TimeUnit.MILLISECONDS);
    }


    /**
     * {@inheritDoc}
     * @param id 待调度的对象的id
     */
    @Override
    public void unschedule(String id) {
        scheduling.remove(id);
    }


    /**
     * {@inheritDoc}
     * @param id 调度的对象 id
     * @return 是否调度中
     */
    @Override
    public boolean isScheduling(String id) {
        return scheduling.containsKey(id);
    }

}
