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
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.meta.info.Plan;
import org.limbo.flowjob.broker.core.meta.info.PlanRepository;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/5/8
 */
@Repository
public class PlanRepo implements PlanRepository {

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Override
    public Plan get(String id) {
        PlanEntity planEntity = planEntityRepo.findById(id).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN + id));
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(planEntity.getCurrentVersion())
                .orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INFO + planEntity.getCurrentVersion()));
        return assemble(planEntity, planInfoEntity);
    }

    @Override
    @Transactional
    public Plan lockAndGet(String id) {
        PlanEntity planEntity = planEntityRepo.selectForUpdate(id);
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(planEntity.getCurrentVersion())
                .orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INFO + planEntity.getCurrentVersion()));
        return assemble(planEntity, planInfoEntity);
    }

    @Override
    public Plan getByVersion(String id, String version) {
        PlanEntity planEntity = planEntityRepo.findById(id).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN + id));
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(version)
                .orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INFO + planEntity.getCurrentVersion()));
        if (!Objects.equals(planInfoEntity.getPlanId(), planEntity.getPlanId())) {
            throw new IllegalArgumentException("plan:" + id + " version:" + version + " not match");
        }
        return assemble(planEntity, planInfoEntity);
    }

    @Override
    public List<Plan> loadUpdatedPlans(URL brokerUrl, LocalDateTime updatedAt) {
        List<PlanEntity> planEntities = planEntityRepo.loadUpdatedPlans(brokerUrl.toString(), updatedAt);
        if (CollectionUtils.isEmpty(planEntities)) {
            return Collections.emptyList();
        }
        List<String> versions = planEntities.stream().map(PlanEntity::getCurrentVersion).collect(Collectors.toList());
        List<PlanInfoEntity> planInfoEntities = planInfoEntityRepo.findAllById(versions);
        Map<String, PlanInfoEntity> planInfoEntityMap = planInfoEntities.stream().collect(Collectors.toMap(PlanInfoEntity::getPlanInfoId, p -> p));
        List<Plan> list = new ArrayList<>();
        for (PlanEntity planEntity : planEntities) {
            list.add(assemble(planEntity, planInfoEntityMap.get(planEntity.getCurrentVersion())));
        }
        return list;
    }

    @Override
    public Map<String, URL> findNotInBrokers(List<URL> brokerUrls, int limit) {
        List<String> urls = brokerUrls.stream().map(URL::toString).collect(Collectors.toList());
        List<PlanEntity> planEntities = planEntityRepo.findNotInBrokers(urls, limit);
        if (CollectionUtils.isEmpty(planEntities)) {
            return Collections.emptyMap();
        }
        Map<String, URL> map = new HashMap<>();
        for (PlanEntity planEntity : planEntities) {
            map.put(planEntity.getPlanId(), DomainConverter.brokerUrl(planEntity.getBrokerUrl()));
        }
        return map;
    }

    @Override
    @Transactional
    public boolean updateBroker(String id, URL oldBrokerUrl, URL newBrokerUrl) {
        String oldStr = oldBrokerUrl == null ? "" : oldBrokerUrl.toString();
        String newStr = newBrokerUrl == null ? "" : newBrokerUrl.toString();
        return planEntityRepo.updateBroker(id, oldStr, newStr) > 0;
    }

    private Plan assemble(PlanEntity planEntity, PlanInfoEntity planInfoEntity) {
        InstanceType instanceType = InstanceType.parse(planInfoEntity.getPlanType());
        TriggerType triggerType = TriggerType.parse(planInfoEntity.getTriggerType());

        // 获取最近一次调度的planInstance和最近一次结束的planInstance
        ScheduleOption scheduleOption = DomainConverter.toScheduleOption(planInfoEntity);

        PlanInstanceEntity latelyTrigger = planInstanceEntityRepo.findLatelyTrigger(planEntity.getPlanId(), planEntity.getCurrentVersion(), scheduleOption.getScheduleType().type, triggerType.type);
        PlanInstanceEntity latelyFeedback = planInstanceEntityRepo.findLatelyFeedback(planEntity.getPlanId(), planEntity.getCurrentVersion(), scheduleOption.getScheduleType().type, triggerType.type);

        LocalDateTime latelyTriggerAt = latelyTrigger == null || latelyTrigger.getTriggerAt() == null ? null : latelyTrigger.getTriggerAt().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime latelyFeedbackAt = latelyFeedback == null || latelyFeedback.getFeedbackAt() == null ? null : latelyFeedback.getFeedbackAt().truncatedTo(ChronoUnit.SECONDS);

        DAG<WorkflowJobInfo> dag;
        if (InstanceType.STANDALONE == instanceType) {
            WorkflowJobInfo jobInfo = JacksonUtils.parseObject(planInfoEntity.getJobInfo(), WorkflowJobInfo.class);
            dag = new DAG<>(Collections.singletonList(jobInfo));
        } else {
            dag = DomainConverter.toJobDag(planInfoEntity.getJobInfo());
        }

        return Plan.builder()
                .id(planInfoEntity.getPlanId())
                .version(planInfoEntity.getPlanInfoId())
                .type(instanceType)
                .triggerType(triggerType)
                .scheduleOption(scheduleOption)
                .dag(dag)
                .latelyTriggerAt(latelyTriggerAt)
                .latelyFeedbackAt(latelyFeedbackAt)
                .brokerUrl(DomainConverter.brokerUrl(planEntity.getBrokerUrl()))
                .enabled(planEntity.isEnabled())
                .build();
    }

}
