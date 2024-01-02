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

package org.limbo.flowjob.broker.dao.repositories;

import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.meta.job.JobInfo;
import org.limbo.flowjob.broker.core.meta.job.JobInstance;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.meta.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public void save(JobInstance jobInstance) {
        JobInstanceEntity jobInstanceEntity = DomainConverter.toJobInstanceEntity(jobInstance);
        jobInstanceEntityRepo.saveAndFlush(jobInstanceEntity);
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

    @Override
    public List<JobInstance> findByExecuteCheck(URL brokerUrl, LocalDateTime lastReportAtStart, LocalDateTime lastReportAtEnd, String startId, Integer limit) {
        List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findByExecuteCheck(brokerUrl.toString(), lastReportAtStart, lastReportAtEnd, startId, limit);
        if (CollectionUtils.isEmpty(jobInstanceEntities)) {
            return Collections.emptyList();
        }
        return getByEntities(jobInstanceEntities);
    }

    @Override
    public List<JobInstance> findInSchedule(URL brokerUrl, LocalDateTime lastReportAt, LocalDateTime triggerAt, String startId, Integer limit) {
        List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findInSchedule(brokerUrl.toString(), lastReportAt, triggerAt, startId, limit);
        if (CollectionUtils.isEmpty(jobInstanceEntities)) {
            return Collections.emptyList();
        }
        return getByEntities(jobInstanceEntities);
    }

    @Override
    public JobInstance getIdByBroker(URL brokerUrl) {
        JobInstanceEntity entity = jobInstanceEntityRepo.findOneByBrokerUrl(brokerUrl.toString());
        return getByEntity(entity);
    }

    @Override
    public boolean updateBroker(JobInstance instance, URL newBrokerUrl) {
        return jobInstanceEntityRepo.updateBroker(instance.getId(), instance.getBrokerUrl().toString(), newBrokerUrl.toString()) > 0;
    }

    private JobInstance getByEntity(JobInstanceEntity entity) {
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(entity.getPlanInfoId())
                .orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INSTANCE + entity.getPlanId()));
        return assemble(entity, planInfoEntity);
    }

    private List<JobInstance> getByEntities(List<JobInstanceEntity> entities) {
        Set<String> planInfoIds = entities.stream().map(JobInstanceEntity::getPlanInfoId).collect(Collectors.toSet());
        List<PlanInfoEntity> planInfoEntities = planInfoEntityRepo.findAllById(planInfoIds);
        Map<String, PlanInfoEntity> planInfoEntityMap = planInfoEntities.stream().collect(Collectors.toMap(PlanInfoEntity::getPlanInfoId, e -> e));
        return entities.stream().map(e -> assemble(e, planInfoEntityMap.get(e.getPlanInfoId()))).collect(Collectors.toList());
    }

    private JobInstance assemble(JobInstanceEntity entity, PlanInfoEntity planInfoEntity) {
        PlanType planType = PlanType.parse(planInfoEntity.getPlanType());
        WorkflowJobInfo jobInfo;
        if (PlanType.STANDALONE == planType) {
            jobInfo = JacksonUtils.parseObject(planInfoEntity.getJobInfo(), WorkflowJobInfo.class);
        } else if (PlanType.WORKFLOW == planType) {
            DAG<WorkflowJobInfo> dag = DomainConverter.toJobDag(planInfoEntity.getJobInfo());
            jobInfo = dag.getNode(entity.getJobId());
        } else {
            throw new IllegalArgumentException("Illegal PlanType in plan:" + planInfoEntity.getPlanId() + " version:" + planInfoEntity.getPlanInfoId());
        }

        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(entity.getPlanInstanceId()).orElse(null);
        Attributes attributes = new Attributes();
        attributes.put(new Attributes(planInstanceEntity.getAttributes()));
        attributes.put(jobInfo.getAttributes());
        return JobInstance.builder()
                .id(entity.getJobInstanceId())
                .jobInfo(jobInfo)
                .planType(planType)
                .planId(entity.getPlanId())
                .retryTimes(entity.getRetryTimes())
                .planInstanceId(entity.getPlanInstanceId())
                .planVersion(entity.getPlanInfoId())
                .brokerUrl(DomainConverter.brokerUrl(entity.getBrokerUrl()))
                .triggerAt(entity.getTriggerAt())
                .context(new Attributes(entity.getContext()))
                .attributes(attributes)
                .status(JobStatus.parse(entity.getStatus()))
                .startAt(entity.getStartAt())
                .endAt(entity.getEndAt())
                .build();
    }

}
