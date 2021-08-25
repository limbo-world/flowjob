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

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.JobNodeType;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.schedule.executor.Executor;
import org.limbo.flowjob.tracker.core.storage.JobInstanceStorage;

import java.util.List;

/**
 *
 * 计划执行器
 * 生成 plan 以及 job 的实例 并保存
 * @author Brozen
 * @since 2021-07-13
 */
public class PlanExecutor implements Executor<Plan> {

    private final PlanInstanceRepository planInstanceRepository;

    private final JobInstanceStorage jobInstanceStorage;

    public PlanExecutor(PlanInstanceRepository planInstanceRepository, JobInstanceStorage jobInstanceStorage) {
        this.planInstanceRepository = planInstanceRepository;
        this.jobInstanceStorage = jobInstanceStorage;
    }

    /**
     * 生成 planInstance 存入 db
     * 获取 plan 中需要最先执行的 job
     * 生成 jobInstance 存入 db
     * todo 事务
     * @param plan 计划
     */
    @Override
    public void execute(Plan plan) {
        // 校验能否下发
        List<Job> jobs = plan.getEarliestJobs();
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }
        if (jobs.size() == 1 && JobNodeType.END == jobs.get(0).getNodeType()) {
            return;
        }

        // 判断是这个计划 第几次调度 决定 实例ID
        Long planInstanceId = planInstanceRepository.createId(plan.getPlanId());

        // 持久化存储
        PlanInstance planInstance = plan.newInstance(planInstanceId, PlanScheduleStatus.Scheduling,
                ScheduleType.FIXED_INTERVAL == plan.getScheduleOption().getScheduleType());
        planInstanceRepository.addInstance(planInstance);

        // 下发需要最先执行的job
        for (Job job : jobs) {
            jobInstanceStorage.store(job.newInstance(plan.getPlanId(), planInstanceId, plan.getVersion(), JobScheduleStatus.Scheduling));
        }

    }


}
