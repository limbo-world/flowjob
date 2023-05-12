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

package org.limbo.flowjob.broker.dao.domain;

import lombok.Setter;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

/**
 * @author Devil
 * @since 2023/5/8
 */
@Repository
public class JobInstanceRepo implements JobInstanceRepository {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Override
    public JobInstance get(String id) {
        JobInstanceEntity entity = jobInstanceEntityRepo.findById(id).orElse(null);
        if (entity == null) {
            return null;
        } else {
            return getByEntity(entity);
        }
    }

    @Override
    public JobInstance getLatest(String planInstanceId, String jobId) {
        JobInstanceEntity entity = jobInstanceEntityRepo.findByLatest(planInstanceId, jobId);
        if (entity == null) {
            return null;
        } else {
            return getByEntity(entity);
        }
    }

    private JobInstance getByEntity(JobInstanceEntity entity) {
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(entity.getPlanInfoId())
                .orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INSTANCE + entity.getPlanId()));

        JobInstance jobInstance = new JobInstance();
        PlanType planType = PlanType.parse(planInfoEntity.getPlanType());
        JobInfo jobInfo;
        if (PlanType.NORMAL == planType) {
            jobInfo = JacksonUtils.parseObject(planInfoEntity.getJobInfo(), JobInfo.class);
        } else if (PlanType.WORKFLOW == planType) {
            DAG<WorkflowJobInfo> dag = DomainConverter.toJobDag(planInfoEntity.getJobInfo());
            jobInfo = dag.getNode(entity.getJobId());
        } else {
            throw new IllegalArgumentException("Illegal PlanType in plan:" + planInfoEntity.getPlanId() + " version:" + planInfoEntity.getPlanInfoId());
        }

        jobInstance.setJobInfo(jobInfo);
        jobInstance.setPlanType(planType);
        jobInstance.setPlanId(entity.getPlanId());
        jobInstance.setRetryTimes(entity.getRetryTimes());
        jobInstance.setPlanInstanceId(entity.getPlanInstanceId());
        jobInstance.setPlanVersion(entity.getPlanInstanceId());
        jobInstance.setStatus(JobStatus.SCHEDULING);
        jobInstance.setTriggerAt(entity.getTriggerAt());
        jobInstance.setContext(new Attributes(entity.getContext()));
        jobInstance.setJobAttributes(jobInfo.getAttributes() == null ? new Attributes() : jobInfo.getAttributes());
        jobInstance.setJobInstanceId(entity.getJobInstanceId());
        jobInstance.setStatus(JobStatus.parse(entity.getStatus()));
        jobInstance.setStartAt(entity.getStartAt());
        jobInstance.setEndAt(entity.getEndAt());
        return jobInstance;
    }
}
