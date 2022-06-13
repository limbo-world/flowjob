/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.dao.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.plan.PlanInstanceContext;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceContextRepository;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.dao.converter.PlanInstancePoConverter;
import org.limbo.flowjob.broker.dao.mybatis.PlanInstanceMapper;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceContextEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Repository
public class MyBatisPlanInstanceRepoContext implements PlanInstanceContextRepository {

    @Autowired
    private PlanInstanceMapper planInstanceMapper;

    @Autowired
    private PlanInstancePoConverter converter;


    /**
     * {@inheritDoc}
     * @param instance 实例
     */
    @Override
    public void add(PlanInstanceContext instance) {
        PlanInstanceContextEntity po = converter.convert(instance);
        planInstanceMapper.insert(po);
    }


    @Override
    public void end(PlanInstanceContext.ID planInstanceId, PlanScheduleStatus state) {
        planInstanceMapper.update(null, Wrappers.<PlanInstanceContextEntity>lambdaUpdate()
                .set(PlanInstanceContextEntity::getState, state.status)
                .set(PlanInstanceContextEntity::getEndAt, TimeUtil.nowLocalDateTime())
                .eq(PlanInstanceContextEntity::getPlanId, planInstanceId.planId)
                .eq(PlanInstanceContextEntity::getPlanRecordId, planInstanceId.planRecordId)
                .eq(PlanInstanceContextEntity::getPlanInstanceId, planInstanceId.planInstanceId)
                .eq(PlanInstanceContextEntity::getState, PlanScheduleStatus.SCHEDULING.status)
        );
    }


    /**
     * {@inheritDoc}
     * @param planInstanceId 计划实例ID
     * @return
     */
    @Override
    public PlanInstanceContext get(PlanInstanceContext.ID planInstanceId) {
        PlanInstanceContextEntity po = planInstanceMapper.selectOne(Wrappers.<PlanInstanceContextEntity>lambdaQuery()
                .eq(PlanInstanceContextEntity::getPlanId, planInstanceId.planId)
                .eq(PlanInstanceContextEntity::getPlanRecordId, planInstanceId.planRecordId)
                .eq(PlanInstanceContextEntity::getPlanInstanceId, planInstanceId.planInstanceId)
        );
        return converter.reverse().convert(po);
    }


    /**
     * {@inheritDoc}
     * @param planRecordId 计划执行记录ID
     * @return
     */
    @Override
    public List<PlanInstanceContext> list(PlanInstance.ID planRecordId) {
        List<PlanInstanceContext> result = new ArrayList<>();
        List<PlanInstanceContextEntity> pos = planInstanceMapper.selectList(Wrappers.<PlanInstanceContextEntity>lambdaQuery()
                .eq(PlanInstanceContextEntity::getPlanId, planRecordId.planId)
                .eq(PlanInstanceContextEntity::getPlanRecordId, planRecordId.planRecordId)
        );
        if (CollectionUtils.isEmpty(pos)) {
            return result;
        }
        for (PlanInstanceContextEntity po : pos) {
            result.add(converter.reverse().convert(po));
        }
        return result;
    }


    @Override
    public PlanInstanceContext.ID createId(PlanInstance.ID planRecordId) {
        Integer recentlyIdForUpdate = planInstanceMapper
                .getRecentlyIdForUpdate(planRecordId.planId, planRecordId.planRecordId);
        int planInstanceId = recentlyIdForUpdate == null ? 1 : recentlyIdForUpdate + 1;
        return new PlanInstanceContext.ID(
                planRecordId.planId,
                planRecordId.planRecordId,
                planInstanceId
        );
    }

}
