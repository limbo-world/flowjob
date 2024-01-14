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

package org.limbo.flowjob.broker.core.meta.task;

import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.meta.info.Plan;
import org.limbo.flowjob.broker.core.meta.info.PlanRepository;
import org.limbo.flowjob.broker.core.meta.job.JobInstance;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.meta.lock.DistributedLock;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixDelayMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.common.thread.CommonThreadPool;

import java.time.Duration;

/**
 * @author Devil
 * @since 2024/1/13
 */
public class RebalanceTask extends FixDelayMetaTask {

    private final NodeManger nodeManger;

    private final DistributedLock lock;

    private final PlanRepository planRepository;

    private final JobInstanceRepository jobInstanceRepository;

    public RebalanceTask(MetaTaskScheduler metaTaskScheduler,
                         NodeManger nodeManger,
                         DistributedLock lock,
                         PlanRepository planRepository,
                         JobInstanceRepository jobInstanceRepository) {
        super(Duration.ofSeconds(5), metaTaskScheduler);
        this.nodeManger = nodeManger;
        this.lock = lock;
        this.planRepository = planRepository;
        this.jobInstanceRepository = jobInstanceRepository;
    }


    @Override
    protected void executeTask() {
        // plan rebalance
        CommonThreadPool.IO.submit(() -> {
            Plan plan = planRepository.getOneByBroker(url);
            while (plan != null) {
                // 如果重新上线了需要忽略
                if (alive(url.toString())) {
                    break;
                }

                planRepository.updateBroker(plan, url);

                plan = planRepository.getOneByBroker(url);
            }
        });

        // job rebalance
        CommonThreadPool.IO.submit(() -> {
            JobInstance instance = jobInstanceRepository.getOneByBroker(url);
            while (instance != null) {
                // 如果重新上线了需要忽略
                if (alive(url.toString())) {
                    break;
                }

                jobInstanceRepository.updateBroker(instance, url);

                instance = jobInstanceRepository.getOneByBroker(url);
            }
        });
    }

    @Override
    public String getType() {
        return "Rebalance";
    }

    @Override
    public String getMetaId() {
        return this.getClass().getSimpleName();
    }
}
