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
import org.limbo.flowjob.broker.application.plan.component.JobScheduler;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.JobInstanceFactory;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.PlanStatus;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.dag.DAGNode;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            // 当前节点为叶子节点 检测 Plan 实例是否已经执行完成
            // 1. 所有节点都已经成功或者失败 2. 这里只关心plan的成功更新，失败是在task回调
            if (checkJobsSuccessOrIgnoreError(planInstance.getPlanInstanceId(), dag.lasts())) {
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
                // 前置节点已经完成则可以下发
                if (checkJobsSuccessOrIgnoreError(planInstance.getPlanInstanceId(), dag.preNodes(subJobInfo.getId()))) {
                    subJobInstances.add(JobInstanceFactory.create(planInstance, subJobInfo, TimeUtils.currentLocalDateTime()));
                }
            }

            jobInstanceRepository.saveAll(subJobInstances);

            for (JobInstance subJobInstance : subJobInstances) {
                scheduler.schedule(subJobInstance);
            }

        }
    }

    /**
     * 校验 planInstance 下对应 job 的 jobInstance 是否执行成功 或者失败了但是可以忽略失败
     * @param planInstanceId
     * @param jobInfos
     * @return
     */
    private boolean checkJobsSuccessOrIgnoreError(String planInstanceId, List<JobInfo> jobInfos) {
        if (CollectionUtils.isEmpty(jobInfos)) {
            return true;
        }
        Map<String, JobInfo> jobInfoMap = jobInfos.stream().collect(Collectors.toMap(DAGNode::getId, jobInfo -> jobInfo));
        List<JobInstanceEntity> entities = jobInstanceEntityRepo.findByPlanInstanceIdAndJobIdIn(Long.valueOf(planInstanceId), new LinkedList<>(jobInfoMap.keySet()));
        if (CollectionUtils.isEmpty(entities) || jobInfos.size() > entities.size()) {
            return false; // 有些job还未创建
        }
        for (JobInstanceEntity entity : entities) {
            if (entity.getStatus() == JobStatus.SUCCEED.status) {
                // 成功的
            } else if (entity.getStatus() == JobStatus.FAILED.status) {
                // 失败的 看是否忽略失败
                JobInfo jobInfo = jobInfoMap.get(entity.getJobId());
                if (jobInfo.isTerminateWithFail()) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }
}
