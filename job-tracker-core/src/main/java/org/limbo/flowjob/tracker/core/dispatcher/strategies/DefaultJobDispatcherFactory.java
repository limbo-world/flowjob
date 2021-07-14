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

package org.limbo.flowjob.tracker.core.dispatcher.strategies;

import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.flowjob.tracker.commons.constants.enums.DispatchType;
import org.limbo.flowjob.tracker.commons.exceptions.JobExecuteException;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;

/**
 * 默认{@link JobDispatcher} 工厂类，从{@link JobContext}中读取{@link Job}，并根据job的{@link DispatchType}创建作业分发器。
 *
 * @author Brozen
 * @since 2021-05-18
 */
public class DefaultJobDispatcherFactory implements JobDispatcherFactory {

    /**
     * 作业计划repo
     */
    private PlanRepository planRepository;

    public DefaultJobDispatcherFactory(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    /**
     * Double Dispatch (￣▽￣)~* <br/>
     * 根据作业的分发方式，创建一个分发器实例。委托给{@link DispatchType}执行。
     *
     * @param tracker tracker节点
     * @param context 作业上下文
     * @return 分发器实例
     */
    public JobDispatcher newDispatcher(JobTracker tracker, JobContext context) {
        Plan plan = planRepository.getPlan(context.getPlanId());
        Job job = plan.getJob(context.getJobId());

        DispatchOption dispatchOption = job.getDispatchOption();
        // job未指定分发策略时，使用plan的分发策略
        if (dispatchOption == null) {
            dispatchOption = plan.getDispatchOption();
        }

        DispatchType dispatchType = dispatchOption.getDispatchType();
        switch (dispatchType) {
            case ROUND_ROBIN:
                return new RoundRobinJobDispatcher();

            case RANDOM:
                return new RandomJobDispatcher();

            case LEAST_FREQUENTLY_USED:
                return new LFUJobDispatcher();

            case LEAST_RECENTLY_USED:
                return new LRUJobDispatcher();

            case APPOINT:
                return new AppointJobDispatcher();

            case CONSISTENT_HASH:
                return new ConsistentHashJobDispatcher();

            default:
                throw new JobExecuteException(context.getJobId(),
                        "Cannot create JobDispatcher for dispatch type: " + dispatchType);
        }
    }

}
