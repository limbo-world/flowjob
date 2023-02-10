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

package org.limbo.flowjob.broker.application.component.schedule;

import lombok.Setter;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.SingleJobInstance;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInstance;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2023/2/9
 */
@Component
public class JobInstanceHelper {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    public JobInstance getJobInstance(String id) {
        JobInstanceEntity entity = jobInstanceEntityRepo.findById(id).orElse(null);
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(entity.getPlanInfoId()).orElse(null);

        JobInstance jobInstance;
        PlanType planType = PlanType.parse(planInfoEntity.getPlanType());
        if (PlanType.SINGLE == planType) {
            jobInstance = new SingleJobInstance();
            JobInfo jobInfo = JacksonUtils.parseObject(planInfoEntity.getJobInfo(), JobInfo.class);
            ((SingleJobInstance) jobInstance).setJobInfo(jobInfo);
        } else if (PlanType.WORKFLOW == planType) {
            jobInstance = new WorkflowJobInstance();
            DAG<WorkflowJobInfo> dag = DomainConverter.toJobDag(planInfoEntity.getJobInfo());
            ((WorkflowJobInstance) jobInstance).setWorkflowJobInfo(dag.getNode(entity.getJobId()));
        } else {
            throw new IllegalArgumentException("Illegal PlanType in plan:" + planInfoEntity.getPlanId() + " version:" + planInfoEntity.getPlanInfoId());
        }
        JobInfo jobInfo = jobInstance.getJobInfo();
        jobInstance.setJobInstanceId(entity.getJobInstanceId());
        wrapJobInstance(jobInstance, entity.getPlanId(), entity.getPlanInfoId(), planType,
                entity.getPlanInstanceId(), new Attributes(entity.getContext()), jobInfo.getAttributes(), entity.getTriggerAt()
        );
        jobInstance.setStatus(JobStatus.parse(entity.getStatus()));
        jobInstance.setStartAt(entity.getStartAt());
        jobInstance.setEndAt(entity.getEndAt());
        return jobInstance;
    }

    public boolean needRetry(JobInstance jobInstance) {
        JobInfo jobInfo = jobInstance.getJobInfo();
        // 查询已经失败的记录数
        long retry = jobInstanceEntityRepo.countByPlanInstanceIdAndJobId(jobInstance.getPlanInstanceId(), jobInfo.getId());
        return jobInfo.getRetryOption().getRetry() > retry;
    }

    public JobInstance newWorkflowJobInstance(String planId, String planVersion, PlanType planType, String planInstanceId,
                                              Attributes context, WorkflowJobInfo workflowJobInfo, LocalDateTime triggerAt) {
        JobInfo job = workflowJobInfo.getJob();
        String jobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
        WorkflowJobInstance instance = new WorkflowJobInstance();
        instance.setJobInstanceId(jobInstanceId);
        instance.setWorkflowJobInfo(workflowJobInfo);
        wrapJobInstance(instance, planId, planVersion, planType, planInstanceId, context, job.getAttributes(), triggerAt);
        return instance;
    }

    public void wrapJobInstance(JobInstance instance, String planId, String planVersion, PlanType planType,
                                String planInstanceId, Attributes context, Attributes jobAttributes, LocalDateTime triggerAt) {

        instance.setPlanId(planId);
        instance.setPlanInstanceId(planInstanceId);
        instance.setPlanVersion(planVersion);
        instance.setPlanType(planType);
        instance.setStatus(JobStatus.SCHEDULING);
        instance.setTriggerAt(triggerAt);
        instance.setContext(context == null ? new Attributes() : context);
        instance.setJobAttributes(jobAttributes == null ? new Attributes() : jobAttributes);
    }
}
