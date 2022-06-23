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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.Setter;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.dao.converter.PlanRecordPoConverter;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.mybatis.PlanInstanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Repository
public class MyBatisPlanInstanceRepo implements PlanInstanceRepository {

    @Autowired
    private PlanInstanceMapper planInstanceMapper;

    @Autowired
    private PlanRecordPoConverter converter;

    @Setter(onMethod_ = @Inject)
    private IDRepo idRepo;


    @Override
    @Transactional
    public String add(PlanInstance instance) {
        String planInstanceId = idRepo.createPlanInstanceId();

        instance.setPlanInstanceId(planInstanceId);

        PlanInstanceEntity po = converter.convert(instance);
        planInstanceMapper.insert(po);

        return planInstanceId;
    }


    @Override
    public PlanInstance get(String planInstanceId) {
        PlanInstanceEntity po = planInstanceMapper.selectOne(Wrappers.<PlanInstanceEntity>lambdaQuery()
                .eq(PlanInstanceEntity::getPlanInstanceId, planInstanceId)
        );
        return converter.reverse().convert(po);
    }


    @Override
    public void end(String planInstanceId, PlanScheduleStatus state) {
        planInstanceMapper.update(null, Wrappers.<PlanInstanceEntity>lambdaUpdate()
                .set(PlanInstanceEntity::getState, state.status)
                .set(PlanInstanceEntity::getEndAt, TimeUtil.nowLocalDateTime())
                .eq(PlanInstanceEntity::getPlanInstanceId, planInstanceId)
                .eq(PlanInstanceEntity::getState, PlanScheduleStatus.SCHEDULING.status)
        );
    }

}
