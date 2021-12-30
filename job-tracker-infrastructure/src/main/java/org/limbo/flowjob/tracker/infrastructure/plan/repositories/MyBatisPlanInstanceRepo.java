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

package org.limbo.flowjob.tracker.infrastructure.plan.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.tracker.core.plan.PlanInstanceRepository;
import org.limbo.flowjob.tracker.core.plan.PlanRecord;
import org.limbo.flowjob.tracker.dao.mybatis.PlanInstanceMapper;
import org.limbo.flowjob.tracker.dao.po.PlanInstancePO;
import org.limbo.flowjob.tracker.infrastructure.plan.converters.PlanInstancePoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Repository
public class MyBatisPlanInstanceRepo implements PlanInstanceRepository {

    @Autowired
    private PlanInstanceMapper planInstanceMapper;

    @Autowired
    private PlanInstancePoConverter converter;


    /**
     * {@inheritDoc}
     * @param instance 实例
     */
    @Override
    public void add(PlanInstance instance) {
        PlanInstancePO po = converter.convert(instance);
        planInstanceMapper.insert(po);
    }


    @Override
    public void end(PlanInstance.ID planInstanceId, PlanScheduleStatus state) {
        planInstanceMapper.update(null, Wrappers.<PlanInstancePO>lambdaUpdate()
                .set(PlanInstancePO::getState, state.status)
                .set(PlanInstancePO::getEndAt, TimeUtil.nowLocalDateTime())
                .eq(PlanInstancePO::getPlanId, planInstanceId.planId)
                .eq(PlanInstancePO::getPlanRecordId, planInstanceId.planRecordId)
                .eq(PlanInstancePO::getPlanInstanceId, planInstanceId.planInstanceId)
                .eq(PlanInstancePO::getState, PlanScheduleStatus.SCHEDULING.status)
        );
    }


    /**
     * {@inheritDoc}
     * @param planInstanceId 计划实例ID
     * @return
     */
    @Override
    public PlanInstance get(PlanInstance.ID planInstanceId) {
        PlanInstancePO po = planInstanceMapper.selectOne(Wrappers.<PlanInstancePO>lambdaQuery()
                .eq(PlanInstancePO::getPlanId, planInstanceId.planId)
                .eq(PlanInstancePO::getPlanRecordId, planInstanceId.planRecordId)
                .eq(PlanInstancePO::getPlanInstanceId, planInstanceId.planInstanceId)
        );
        return converter.reverse().convert(po);
    }


    /**
     * {@inheritDoc}
     * @param planRecordId 计划执行记录ID
     * @return
     */
    @Override
    public List<PlanInstance> list(PlanRecord.ID planRecordId) {
        List<PlanInstance> result = new ArrayList<>();
        List<PlanInstancePO> pos = planInstanceMapper.selectList(Wrappers.<PlanInstancePO>lambdaQuery()
                .eq(PlanInstancePO::getPlanId, planRecordId.planId)
                .eq(PlanInstancePO::getPlanRecordId, planRecordId.planRecordId)
        );
        if (CollectionUtils.isEmpty(pos)) {
            return result;
        }
        for (PlanInstancePO po : pos) {
            result.add(converter.reverse().convert(po));
        }
        return result;
    }


    @Override
    public PlanInstance.ID createId(PlanRecord.ID planRecordId) {
        Integer recentlyIdForUpdate = planInstanceMapper
                .getRecentlyIdForUpdate(planRecordId.planId, planRecordId.planRecordId);
        int planInstanceId = recentlyIdForUpdate == null ? 1 : recentlyIdForUpdate + 1;
        return new PlanInstance.ID(
                planRecordId.planId,
                planRecordId.planRecordId,
                planInstanceId
        );
    }

}
