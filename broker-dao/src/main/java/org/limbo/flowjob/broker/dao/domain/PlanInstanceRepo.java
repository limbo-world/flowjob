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
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.dao.converter.PlanRecordPoConverter;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Repository
public class PlanInstanceRepo implements PlanInstanceRepository {


    @Autowired
    private PlanRecordPoConverter converter;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;


    @Override
    @Transactional
    public String add(PlanInstance instance) {
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

    /**
     * {@inheritDoc}
     *
     * @param instance
     */
    @Override
    public void executeSucceed(PlanInstance instance) {
        planInstanceEntityRepo.end(
                Long.valueOf(instance.getPlanInstanceId()),
                PlanScheduleStatus.EXECUTING.status,
                instance.getState().status,
                TimeUtil.nowLocalDateTime()
        );
    }


    /**
     * {@inheritDoc}
     *
     * @param instance
     */
    @Override
    public void executeFailed(PlanInstance instance) {
        planInstanceEntityRepo.end(
                Long.valueOf(instance.getPlanInstanceId()),
                PlanScheduleStatus.EXECUTING.status,
                instance.getState().status,
                TimeUtil.nowLocalDateTime()
        );
    }


}
