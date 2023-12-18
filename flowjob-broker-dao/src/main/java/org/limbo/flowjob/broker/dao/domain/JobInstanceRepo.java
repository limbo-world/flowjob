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
import org.apache.commons.collections4.CollectionUtils;
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
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/5/8
 */
@Repository
public class JobInstanceRepo implements JobInstanceRepository {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

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
    @Transactional
    public void saveAll(List<JobInstance> jobInstances) {
        if (CollectionUtils.isEmpty(jobInstances)) {
            return;
        }

        // 如果是 plan instance 的检测，那么 job instance 已经创建，则无需再度创建

        // 保存 jobInstance
        List<JobInstanceEntity> jobInstanceEntities = jobInstances.stream().map(DomainConverter::toJobInstanceEntity).collect(Collectors.toList());
        jobInstanceEntityRepo.saveAll(jobInstanceEntities);
        jobInstanceEntityRepo.flush();
    }

    @Override
    @Transactional
    public boolean executing(String jobInstanceId, String agentId, LocalDateTime startAt) {
        return jobInstanceEntityRepo.executing(jobInstanceId, agentId, TimeUtils.currentLocalDateTime()) > 0;
    }

    @Override
    @Transactional
    public boolean success(String jobInstanceId, LocalDateTime endAt, String context) {
        return jobInstanceEntityRepo.success(jobInstanceId, endAt, context) > 0;
    }

    @Override
    @Transactional
    public boolean fail(String jobInstanceId, Integer oldStatus, LocalDateTime startAt, LocalDateTime endAt, String errorMsg) {
        return jobInstanceEntityRepo.fail(jobInstanceId, oldStatus, startAt, endAt, errorMsg) > 0;
    }

    @Override
    @Transactional
    public boolean report(String jobInstanceId, LocalDateTime lastReportAt) {
        return jobInstanceEntityRepo.report(jobInstanceId, lastReportAt) > 0;
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
        if (PlanType.STANDALONE == planType) {
            jobInfo = JacksonUtils.parseObject(planInfoEntity.getJobInfo(), JobInfo.class);
        } else if (PlanType.WORKFLOW == planType) {
            DAG<WorkflowJobInfo> dag = DomainConverter.toJobDag(planInfoEntity.getJobInfo());
            jobInfo = dag.getNode(entity.getJobId());
        } else {
            throw new IllegalArgumentException("Illegal PlanType in plan:" + planInfoEntity.getPlanId() + " version:" + planInfoEntity.getPlanInfoId());
        }

        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(entity.getPlanInstanceId()).orElse(null);

        jobInstance.setJobInfo(jobInfo);
        jobInstance.setPlanType(planType);
        jobInstance.setPlanId(entity.getPlanId());
        jobInstance.setRetryTimes(entity.getRetryTimes());
        jobInstance.setPlanInstanceId(entity.getPlanInstanceId());
        jobInstance.setPlanVersion(entity.getPlanInfoId());
        jobInstance.setStatus(JobStatus.SCHEDULING);
        jobInstance.setTriggerAt(entity.getTriggerAt());
        jobInstance.setContext(new Attributes(entity.getContext()));

        Attributes attributes = new Attributes();
        attributes.put(new Attributes(planInstanceEntity.getAttributes()));
        attributes.put(jobInfo.getAttributes());
        jobInstance.setAttributes(attributes);

        jobInstance.setJobInstanceId(entity.getJobInstanceId());
        jobInstance.setStatus(JobStatus.parse(entity.getStatus()));
        jobInstance.setStartAt(entity.getStartAt());
        jobInstance.setEndAt(entity.getEndAt());
        return jobInstance;
    }
}
