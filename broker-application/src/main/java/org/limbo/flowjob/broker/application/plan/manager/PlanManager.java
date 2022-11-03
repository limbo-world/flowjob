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

package org.limbo.flowjob.broker.application.plan.manager;

import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.PlanStatus;
import org.limbo.flowjob.broker.application.plan.component.JobScheduler;
import org.limbo.flowjob.broker.core.domain.job.JobInstanceFactory;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Devil
 * @since 2022/9/1
 */
@Component
public class PlanManager {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepository;
    @Setter(onMethod_ = @Inject)
    private JobScheduler scheduler;

    /**
     * 下发后续任务
     */
    @Transactional
    public void dispatchNext(PlanInstance planInstance, String jobId) {
        DAG<JobInfo> dag = planInstance.getDag();
        // 当前节点的子节点
        List<JobInfo> subJobInfos = dag.subNodes(jobId);

        if (CollectionUtils.isEmpty(subJobInfos)) {
            // 检测 Plan 实例是否已经执行完成
            if (checkFinished(planInstance.getPlanInstanceId(), dag.lasts())) {
                planInstanceEntityRepo.end(
                        Long.valueOf(planInstance.getPlanInstanceId()),
                        PlanStatus.EXECUTING.status,
                        PlanStatus.SUCCEED.status,
                        TimeUtils.currentLocalDateTime()
                );
            }
        } else {

            // 后续作业存在，则检测是否可触发，并继续下发作业
            List<JobInstance> subJobInstances = new ArrayList<>();
            for (JobInfo subJobInfo : subJobInfos) {
                if (checkFinished(planInstance.getPlanInstanceId(), dag.preNodes(subJobInfo.getId()))) {
                    subJobInstances.add(JobInstanceFactory.create(planInstance.getPlanInstanceId(), subJobInfo, TimeUtils.currentLocalDateTime()));
                }
            }

            for (JobInstance subJobInstance : subJobInstances) {
                jobInstanceRepository.save(subJobInstance);
            }

            for (JobInstance subJobInstance : subJobInstances) {
                scheduler.schedule(subJobInstance);
            }

        }
    }

    private boolean checkFinished(String planInstanceId, List<JobInfo> jobInfos) {
        if (CollectionUtils.isEmpty(jobInfos)) {
            return true;
        }
        for (JobInfo jobInfo : jobInfos) {
            List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findByPlanInstanceIdAndJobId(Long.valueOf(planInstanceId), Long.valueOf(jobInfo.getId()));
            // todo 获取 JobInstance
            JobInstanceEntity entity = new JobInstanceEntity();
            if (entity.getStatus() == JobStatus.SUCCEED.status) {
                return true;
            }
            if (entity.getStatus() == JobStatus.FAILED.status) {
                return !jobInfo.isTerminateWithFail();
            }
            return false;
        }

        return true;
    }
}
