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

import org.limbo.flowjob.broker.application.plan.service.PlanService;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.schedule.scheduler.HashedWheelTimerScheduler;

import java.util.concurrent.ExecutorService;

/**
 * @author Devil
 * @since 2022/8/18
 */
public class PlanScheduler extends HashedWheelTimerScheduler<PlanInstance> {

    /**
     * 调度线程池
     */
    private final ExecutorService schedulePool;

    private PlanService planService;

    private JobScheduler jobScheduler;

    public PlanScheduler(ExecutorService schedulePool) {
        super();
        this.schedulePool = schedulePool;
    }


    @Override
    protected void doSchedule(PlanInstance planInstance) {
        // 执行调度逻辑
        schedulePool.submit(new Runnable() {
            @Override
            public void run() {
                PlanInstance.JobInstances jobInstances = new PlanInstance.JobInstances(planInstance.getPlanInstanceId(), planInstance.getRootJobs());

                // 保存数据
                planService.saveScheduleInfo(planInstance, jobInstances);

                // 执行调度逻辑
                for (JobInstance instance : jobInstances.getInstances()) {
                    jobScheduler.schedule(instance);
                }

                // 完成后移除
                unschedule(planInstance.scheduleId());
            }
        });
    }
}
