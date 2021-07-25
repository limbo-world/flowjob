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

package org.limbo.flowjob.tracker.core.plan;

import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.core.storage.JobInstanceStorage;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.schedule.executor.Executor;

import java.util.List;

/**
 *
 * 计划执行器
 * 生成 plan 以及 job 的实例 并保存
 * @author Brozen
 * @since 2021-07-13
 */
public class PlanInstanceExecutor implements Executor<PlanInstance> {

    private final PlanInstanceRepository planInstanceRepository;

    private final JobInstanceStorage jobInstanceStorage;

    public PlanInstanceExecutor(PlanInstanceRepository planInstanceRepository, JobInstanceStorage jobInstanceStorage) {
        this.planInstanceRepository = planInstanceRepository;
        this.jobInstanceStorage = jobInstanceStorage;
    }

    /**
     * 生成 planInstance 存入 db
     * 获取 plan 中需要最先执行的 job
     * 生成 jobInstance 存入 db
     * todo 事务
     * @param instance 实例
     */
    @Override
    public void execute(PlanInstance instance) {

        instance.setState(PlanScheduleStatus.Scheduling);
        planInstanceRepository.addInstance(instance);

        // 下发需要最先执行的job
        List<Job> jobs = instance.getEarliestJobs();
        for (Job job : jobs) {
            JobInstance jobInstance = job.newInstance(instance.getPlanId(), instance.getVersion(), instance.getPlanInstanceId());
            jobInstanceStorage.store(jobInstance);
        }

    }

}
