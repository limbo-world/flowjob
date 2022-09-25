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
import org.limbo.flowjob.broker.api.constants.enums.PlanStatus;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Component
public class PlanInstanceRepo implements PlanInstanceRepository {

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private ScheduleCalculatorFactory scheduleCalculatorFactory;


    @Override
    @Transactional
    public String save(PlanInstance instance) {
        PlanInstanceEntity entity = toEntity(instance);
        planInstanceEntityRepo.saveAndFlush(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    public PlanInstance get(String planInstanceId) {
        return planInstanceEntityRepo.findById(Long.valueOf(planInstanceId)).map(this::toPlanInstance).orElse(null);
    }

    private PlanInstanceEntity toEntity(PlanInstance instance) {
        PlanInstanceEntity planInstanceEntity = new PlanInstanceEntity();
        planInstanceEntity.setId(Long.valueOf(instance.getPlanInstanceId()));
        planInstanceEntity.setPlanId(Long.valueOf(instance.getPlanId()));
        planInstanceEntity.setPlanInfoId(Long.valueOf(instance.getVersion()));
        planInstanceEntity.setStatus(instance.getStatus().status);
        planInstanceEntity.setTriggerType(instance.getTriggerType().type);
        planInstanceEntity.setExpectTriggerAt(instance.getExpectTriggerAt());
        planInstanceEntity.setTriggerAt(instance.getTriggerAt());
        planInstanceEntity.setFeedbackAt(instance.getFeedbackAt());
        return planInstanceEntity;
    }

    private PlanInstance toPlanInstance(PlanInstanceEntity entity) {
        PlanInstance planInstance = new PlanInstance();
        planInstance.setPlanInstanceId(entity.getId().toString());
        planInstance.setPlanId(String.valueOf(entity.getPlanId()));
        planInstance.setVersion(entity.getPlanInfoId().toString());
        planInstance.setStatus(PlanStatus.parse(entity.getStatus()));
        planInstance.setExpectTriggerAt(entity.getExpectTriggerAt());
        planInstance.setTriggerType(TriggerType.parse(entity.getTriggerType()));
        planInstance.setTriggerAt(entity.getTriggerAt());
        planInstance.setFeedbackAt(entity.getFeedbackAt());

        planInstance.setStrategyFactory(scheduleCalculatorFactory);

        // 基础信息
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(entity.getPlanInfoId()).get();
        planInstance.setScheduleOption(DomainConverter.toScheduleOption(planInfoEntity));
        planInstance.setDag(DomainConverter.toJobDag(planInfoEntity.getJobs()));

//        // 处理 JobInstance
//        List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findByPlanInstanceId(entity.getId());
//        if (CollectionUtils.isNotEmpty(jobInstanceEntities)) {
//            for (JobInstanceEntity jobInstanceEntity : jobInstanceEntities) {
//                JobInstance jobInstance = DomainConverter.toJobInstance(jobInstanceEntity, planInfoEntity);
//                planInstance.putJobInstance(jobInstance.getJobId(), jobInstance.isCompleted());
//            }
//        }

        return planInstance;
    }

}
