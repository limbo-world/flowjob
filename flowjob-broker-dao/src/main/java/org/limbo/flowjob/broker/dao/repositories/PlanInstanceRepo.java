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
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.ScheduleType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.core.meta.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.meta.plan.PlanInstance;
import org.limbo.flowjob.broker.core.meta.plan.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * @author Devil
 * @since 2023/12/13
 */
@Repository
public class PlanInstanceRepo implements PlanInstanceRepository {

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Override
    public PlanInstance get(String id) {
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(id).orElse(null);
        if (planInstanceEntity == null) {
            return null;
        }
        return assemble(planInstanceEntity);
    }

    @Override
    @Transactional
    public PlanInstance lockAndGet(String id) {
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.selectForUpdate(id);
        if (planInstanceEntity == null) {
            return null;
        }
        return assemble(planInstanceEntity);
    }

    @Override
    public PlanInstance getLatelyTrigger(String planId, String version, ScheduleType scheduleType, TriggerType triggerType) {
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findLatelyTrigger(planId, version, scheduleType.type, triggerType.type);
        return assemble(planInstanceEntity);
    }

    private PlanInstance assemble(PlanInstanceEntity planInstanceEntity) {
        if (planInstanceEntity == null) {
            return null;
        }
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(planInstanceEntity.getPlanInfoId()).orElse(null);
        if (planInfoEntity == null) {
            return PlanInstance.builder()
                    .id(planInstanceEntity.getPlanInstanceId())
                    .build();
        }

        PlanType planType = PlanType.parse(planInfoEntity.getPlanType());

        ScheduleOption scheduleOption = DomainConverter.toScheduleOption(planInfoEntity);
        DAG<WorkflowJobInfo> dag;
        if (PlanType.STANDALONE == planType) {
            WorkflowJobInfo jobInfo = JacksonUtils.parseObject(planInfoEntity.getJobInfo(), WorkflowJobInfo.class);
            dag = new DAG<>(Collections.singletonList(jobInfo));
        } else {
            dag = DomainConverter.toJobDag(planInfoEntity.getJobInfo());
        }

        return PlanInstance.builder()
                .id(planInstanceEntity.getPlanInstanceId())
                .planId(planInstanceEntity.getPlanId())
                .version(planInstanceEntity.getPlanInfoId())
                .status(planInstanceEntity.getStatus())
                .type(planType)
                .triggerType(TriggerType.parse(planInstanceEntity.getTriggerType()))
                .scheduleType(scheduleOption.getScheduleType())
                .dag(dag)
                .attributes(new Attributes(planInstanceEntity.getAttributes()))
                .triggerAt(planInstanceEntity.getTriggerAt())
                .startAt(planInstanceEntity.getStartAt())
                .feedbackAt(planInstanceEntity.getFeedbackAt())
                .build();
    }

    @Override
    @Transactional
    public void save(PlanInstance planInstance) {
        PlanInstanceEntity planInstanceEntity = new PlanInstanceEntity();
        planInstanceEntity.setPlanInstanceId(planInstance.getId());
        planInstanceEntity.setPlanId(planInstance.getPlanId());
        planInstanceEntity.setPlanInfoId(planInstance.getVersion());
        planInstanceEntity.setStatus(planInstance.getStatus());
        planInstanceEntity.setTriggerType(planInstance.getTriggerType().type);
        planInstanceEntity.setScheduleType(planInstance.getScheduleType().type);
        planInstanceEntity.setAttributes(planInstance.getAttributes().toString());
        planInstanceEntity.setTriggerAt(planInstance.getTriggerAt());
        planInstanceEntity.setStartAt(planInstance.getStartAt());
        planInstanceEntity.setFeedbackAt(planInstance.getFeedbackAt());
        planInstanceEntityRepo.saveAndFlush(planInstanceEntity);
    }

    @Override
    @Transactional
    public boolean executing(String planInstanceId, LocalDateTime startAt) {
        return planInstanceEntityRepo.executing(planInstanceId, startAt) > 0;
    }

    @Override
    @Transactional
    public boolean success(String planInstanceId, LocalDateTime feedbackAt) {
        return planInstanceEntityRepo.success(planInstanceId, feedbackAt) > 0;
    }

    @Override
    @Transactional
    public boolean fail(String planInstanceId, LocalDateTime startAt, LocalDateTime feedbackAt) {
        return planInstanceEntityRepo.fail(planInstanceId, startAt, feedbackAt) > 0;
    }
}
