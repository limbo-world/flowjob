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

package org.limbo.flowjob.tracker.core.scheduler;

import io.netty.util.HashedWheelTimer;
import org.limbo.flowjob.tracker.core.dispatcher.JobDispatchService;
import org.limbo.flowjob.tracker.core.dispatcher.JobDispatchServiceFactory;
import org.limbo.flowjob.tracker.core.job.JobDO;
import org.limbo.flowjob.tracker.core.job.context.JobContextDO;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;

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
public abstract class HashedWheelTimerJobScheduler implements JobScheduler {

    private static final Object PLACEHOLDER = new Object();

    /**
     * 全局唯一的JobTracker
     */
    private JobTracker jobTracker;

    /**
     * 作业执行器服务
     */
    private JobDispatchServiceFactory jobDispatchServiceFactory;

    /**
     * 依赖netty的时间轮算法进行作业调度
     */
    private HashedWheelTimer timer;

    /**
     * 所有正在被调度中的
     */
    private Map<String, Object> scheduledJobIds;

    /**
     * 通过JobTracker和JobExecutorService构造一个作业调度器，该调度器基于哈希时间轮算法。
     * @param jobTracker JobTracker，进程内唯一
     * @param jobDispatchServiceFactory 作业执行器工厂
     */
    public HashedWheelTimerJobScheduler(JobTracker jobTracker, JobDispatchServiceFactory jobDispatchServiceFactory) {
        this.jobTracker = jobTracker;
        this.jobDispatchServiceFactory = jobDispatchServiceFactory;

        this.timer = new HashedWheelTimer(NamedThreadFactory.newInstance(this.getClass().getSimpleName() + "-timer-"));
        this.scheduledJobIds = new ConcurrentHashMap<>();
    }


    @Override
    public void schedule(JobDO job) {
        // 如果job不会被触发，则无需加入调度
        long triggerAt = job.nextTriggerAt();
        if (triggerAt <= 0) {
            return;
        }

        // 防止重复调度
        scheduledJobIds.computeIfAbsent(job.getId(), jobId -> {
            doScheduleJob(job, triggerAt);
            return PLACEHOLDER;
        });
    }

    /**
     * 重新调度作业，在作业执行完成后调用该方法，检测作业是否还会触发，会触发则
     * @param job 作业
     */
    protected void rescheduleJob(JobDO job) {
        // 如果job不会被触发，则无需加入调度
        long triggerAt = job.nextTriggerAt();
        if (triggerAt <= 0) {
            return;
        }

        // 已经取消调度了，则不再重新调度作业
        if (!scheduledJobIds.containsKey(job.getId())) {
            return;
        }

        doScheduleJob(job, triggerAt);
    }

    /**
     * 执行作业调度，根据triggerAt计算执行delay，并注册到timer上执行。
     * @param job 待调度的作业
     * @param triggerAt 作业下次被调度执行的时间戳
     */
    private void doScheduleJob(JobDO job, long triggerAt) {
        // 在timer上调度作业执行
        long delay = triggerAt - System.currentTimeMillis();
        this.timer.newTimeout(timeout -> {

            // 执行作业
            executeJob(job);

            // 检测是否需要重新调度
            this.rescheduleJob(job);

        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行作业。通过创建新的作业执行器来执行。
     * @param job 待执行的作业
     */
    private void executeJob(JobDO job) {
        // 生成新的上下文，并交给执行器执行
        JobContextDO context = job.newContext();
        JobDispatchService executorService = jobDispatchServiceFactory.newDispatchService(context);
        executorService.dispatch(jobTracker, context);
    }

}
