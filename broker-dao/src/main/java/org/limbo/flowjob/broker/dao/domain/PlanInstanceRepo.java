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
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.constants.PlanStatus;
import org.limbo.flowjob.common.constants.TriggerType;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

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
    private JobInfoEntityRepo jobInfoEntityRepo;


    @Override
    @Transactional
    public String save(PlanInstance instance) {
        PlanInstanceEntity entity = toEntity(instance);
        planInstanceEntityRepo.saveAndFlush(entity);
        String planInstanceId = entity.getPlanInstanceId();
        instance.setPlanInstanceId(planInstanceId);
        return planInstanceId;
    }

    @Override
    public PlanInstance get(String planInstanceId) {
        return planInstanceEntityRepo.findById(planInstanceId).map(this::toPlanInstance).orElse(null);
    }

    private PlanInstanceEntity toEntity(PlanInstance instance) {
        PlanInstanceEntity planInstanceEntity = new PlanInstanceEntity();
        planInstanceEntity.setPlanInstanceId(instance.getPlanInstanceId());
        planInstanceEntity.setPlanId(instance.getPlanId());
        planInstanceEntity.setPlanVersion(instance.getVersion());
        planInstanceEntity.setStatus(instance.getStatus().status);
        planInstanceEntity.setTriggerType(instance.getTriggerType().type);
        planInstanceEntity.setExpectTriggerAt(instance.getExpectTriggerAt());
        planInstanceEntity.setTriggerAt(instance.getTriggerAt());
        planInstanceEntity.setFeedbackAt(instance.getFeedbackAt());
        return planInstanceEntity;
    }

    private PlanInstance toPlanInstance(PlanInstanceEntity entity) {
        PlanInstance planInstance = new PlanInstance();
        planInstance.setPlanInstanceId(entity.getPlanInstanceId());
        planInstance.setPlanId(entity.getPlanId());
        planInstance.setVersion(entity.getPlanVersion());
        planInstance.setStatus(PlanStatus.parse(entity.getStatus()));
        planInstance.setExpectTriggerAt(entity.getExpectTriggerAt());
        planInstance.setTriggerType(TriggerType.parse(entity.getTriggerType()));
        planInstance.setTriggerAt(entity.getTriggerAt());
        planInstance.setFeedbackAt(entity.getFeedbackAt());

        // 基础信息
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findByPlanIdAndPlanVersion(entity.getPlanId(), entity.getPlanVersion());
        List<JobInfoEntity> jobInfoEntities = jobInfoEntityRepo.findByPlanInfoId(planInfoEntity.getPlanInfoId());
        planInstance.setScheduleOption(DomainConverter.toScheduleOption(planInfoEntity));
        planInstance.setDag(DomainConverter.toJobDag(planInfoEntity.getJobs(), jobInfoEntities));

//        // 处理 JobInstance todo
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
