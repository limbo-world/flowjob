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
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanRepository;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.broker.dao.converter.PlanInstanceConverter;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Repository
public class PlanInstanceRepo implements PlanInstanceRepository {


    @Setter(onMethod_ = @Inject)
    private PlanInstanceConverter converter;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    // todo 移除
    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepository;

    // todo 移除
    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepository;

    // todo 移除
    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;


    @Override
    @Transactional
    public String save(PlanInstance instance) {
        PlanInstanceEntity entity = converter.convert(instance);
        planInstanceEntityRepo.saveAndFlush(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    public PlanInstance get(String planInstanceId) {
        return planInstanceEntityRepo.findById(Long.valueOf(planInstanceId)).map(entity -> converter.reverse().convert(entity)).orElse(null);
    }

    @Override
    public PlanInstance get(String planId, long expectTriggerTime) {
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findByPlanIdAndExpectTriggerAt(Long.valueOf(planId), expectTriggerTime);
        if (planInstanceEntity == null) {
            return null;
        }
        return converter.reverse().convert(planInstanceEntity);
    }


}
