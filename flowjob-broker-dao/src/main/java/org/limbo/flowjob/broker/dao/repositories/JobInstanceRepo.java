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
import org.limbo.flowjob.api.constants.InstanceType;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.meta.job.JobInstance;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceRepository;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.DelayInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

    @Setter(onMethod_ = @Inject)
    private DelayInstanceEntityRepo delayInstanceEntityRepo;

    @Override
    public JobInstance get(String id) {
        JobInstanceEntity entity = jobInstanceEntityRepo.findById(id).orElse(null);
        if (entity == null) {
            return null;
        } else {
            return assemble(entity);
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
            return assemble(entity);
        }
    }

    @Override
    public List<JobInstance> findByExecuteCheck(URL brokerUrl, LocalDateTime lastReportAtStart, LocalDateTime lastReportAtEnd, String startId, Integer limit) {
        List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findByExecuteCheck(brokerUrl.toString(), lastReportAtStart, lastReportAtEnd, startId, limit);
        if (CollectionUtils.isEmpty(jobInstanceEntities)) {
            return Collections.emptyList();
        }
        return assemble(jobInstanceEntities);
    }

    @Override
    public List<JobInstance> findInSchedule(URL brokerUrl, LocalDateTime lastReportAt, LocalDateTime triggerAt, String startId, Integer limit) {
        List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findInSchedule(brokerUrl.toString(), lastReportAt, triggerAt, startId, limit);
        if (CollectionUtils.isEmpty(jobInstanceEntities)) {
            return Collections.emptyList();
        }
        return assemble(jobInstanceEntities);
    }

    @Override
    public Map<String, URL> findNotInBrokers(List<URL> brokerUrls, int limit) {
        List<String> urls = brokerUrls.stream().map(URL::toString).collect(Collectors.toList());
        List<JobInstanceEntity> entities = jobInstanceEntityRepo.findNotInBrokers(urls, limit);
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyMap();
        }
        Map<String, URL> map = new HashMap<>();
        for (JobInstanceEntity entity : entities) {
            map.put(entity.getJobInstanceId(), DomainConverter.brokerUrl(entity.getBrokerUrl()));
        }
        return map;
    }

    @Override
    @Transactional
    public boolean updateBroker(String id, URL oldBrokerUrl, URL newBrokerUrl) {
        String oldStr = oldBrokerUrl == null ? "" : oldBrokerUrl.toString();
        String newStr = newBrokerUrl == null ? "" : newBrokerUrl.toString();
        return jobInstanceEntityRepo.updateBroker(id, oldStr, newStr) > 0;
    }

    private List<JobInstance> assemble(List<JobInstanceEntity> entities) {
        Map<Integer, List<JobInstanceEntity>> typeGroup = entities.stream().collect(Collectors.groupingBy(JobInstanceEntity::getInstanceType));
        List<JobInstance> list = new ArrayList<>();
        // plan
        List<JobInstanceEntity> planTypeEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(typeGroup.get(InstanceType.STANDALONE.type))) {
            planTypeEntities.addAll(typeGroup.get(InstanceType.STANDALONE.type));
        }
        if (CollectionUtils.isNotEmpty(typeGroup.get(InstanceType.WORKFLOW.type))) {
            planTypeEntities.addAll(typeGroup.get(InstanceType.WORKFLOW.type));
        }
        if (CollectionUtils.isNotEmpty(planTypeEntities)) {
            List<String> instanceIds = planTypeEntities.stream().map(JobInstanceEntity::getInstanceId).collect(Collectors.toList());
            List<PlanInstanceEntity> planInstanceEntities = planInstanceEntityRepo.findAllById(instanceIds);
            Map<String, PlanInstanceEntity> planInstanceEntityMap = planInstanceEntities.stream().collect(Collectors.toMap(PlanInstanceEntity::getPlanInstanceId, e -> e));
            Set<String> planInfoIds = planInstanceEntities.stream().map(PlanInstanceEntity::getPlanInfoId).collect(Collectors.toSet());
            List<PlanInfoEntity> planInfoEntities = planInfoEntityRepo.findAllById(planInfoIds);
            Map<String, PlanInfoEntity> planInfoEntityMap = planInfoEntities.stream().collect(Collectors.toMap(PlanInfoEntity::getPlanInfoId, e -> e));
            list.addAll(planTypeEntities.stream().map(e -> {
                PlanInstanceEntity planInstanceEntity = planInstanceEntityMap.get(e.getInstanceId());
                PlanInfoEntity planInfoEntity = planInfoEntityMap.get(planInstanceEntity.getPlanInfoId());
                return assemble(e, planInstanceEntity.getAttributes(), planInfoEntity.getJobInfo());
            }).collect(Collectors.toList()));
        }
        // delay
        List<JobInstanceEntity> delayTypeEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(typeGroup.get(InstanceType.DELAY_STANDALONE.type))) {
            delayTypeEntities.addAll(typeGroup.get(InstanceType.DELAY_STANDALONE.type));
        }
        if (CollectionUtils.isNotEmpty(delayTypeEntities)) {
            List<String> instanceIds = delayTypeEntities.stream().map(JobInstanceEntity::getInstanceId).collect(Collectors.toList());
            List<DelayInstanceEntity> delayInstanceEntities = delayInstanceEntityRepo.findAllById(instanceIds);
            Map<String, DelayInstanceEntity> map = delayInstanceEntities.stream().collect(Collectors.toMap(DelayInstanceEntity::getInstanceId, v -> v));
            list.addAll(delayTypeEntities.stream().map(e -> {
                DelayInstanceEntity delayInstanceEntity = map.get(e.getInstanceId());
                return assemble(e, delayInstanceEntity.getAttributes(), delayInstanceEntity.getJobInfo());
            }).collect(Collectors.toList()));
        }

        return list;
    }

    private JobInstance assemble(JobInstanceEntity entity) {
        InstanceType instanceType = InstanceType.parse(entity.getInstanceType());
        if (InstanceType.STANDALONE == instanceType || InstanceType.WORKFLOW == instanceType) {
            PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(entity.getInstanceId()).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INSTANCE + entity.getInstanceId()));
            PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(planInstanceEntity.getPlanInfoId()).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INFO + planInstanceEntity.getPlanInfoId()));
            return assemble(entity, planInstanceEntity.getAttributes(), planInfoEntity.getJobInfo());
        } else if (InstanceType.DELAY_STANDALONE == instanceType) {
            DelayInstanceEntity delayInstanceEntity = delayInstanceEntityRepo.findById(entity.getInstanceId()).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_DELAY_INSTANCE + entity.getInstanceId()));
            return assemble(entity, delayInstanceEntity.getAttributes(), delayInstanceEntity.getJobInfo());
        } else {
            return null;
        }
    }

    private JobInstance assemble(JobInstanceEntity entity, String instanceAttributes, String jobInfoJson) {
        InstanceType instanceType = InstanceType.parse(entity.getInstanceType());
        WorkflowJobInfo jobInfo;
        if (InstanceType.STANDALONE == instanceType) {
            jobInfo = JacksonUtils.parseObject(jobInfoJson, WorkflowJobInfo.class);
        } else if (InstanceType.WORKFLOW == instanceType || InstanceType.DELAY_STANDALONE == instanceType) {
            DAG<WorkflowJobInfo> dag = DomainConverter.toJobDag(jobInfoJson);
            jobInfo = dag.getNode(entity.getJobId());
        } else {
            throw new IllegalArgumentException("Illegal InstanceType id:" + entity.getJobInstanceId());
        }

        Attributes attributes = new Attributes();
        attributes.put(new Attributes(instanceAttributes));
        attributes.put(jobInfo.getAttributes());
        return JobInstance.builder()
                .id(entity.getJobInstanceId())
                .agentId(entity.getAgentId())
                .jobInfo(jobInfo)
                .retryTimes(entity.getRetryTimes())
                .instanceId(entity.getInstanceId())
                .instanceType(instanceType)
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
