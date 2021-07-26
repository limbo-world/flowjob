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
import org.limbo.flowjob.tracker.core.plan.Plan;
import org.limbo.flowjob.tracker.core.plan.PlanRepository;
import org.limbo.flowjob.tracker.dao.mybatis.PlanInfoMapper;
import org.limbo.flowjob.tracker.dao.mybatis.PlanMapper;
import org.limbo.flowjob.tracker.dao.po.PlanInfoPO;
import org.limbo.flowjob.tracker.dao.po.PlanPO;
import org.limbo.flowjob.tracker.infrastructure.plan.converters.PlanInfoPOConverter;
import org.limbo.utils.verifies.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Brozen
 * @since 2021-07-13
 */
@Repository
public class MyBatisPlanRepo implements PlanRepository {

    @Autowired
    private PlanInfoMapper planInfoMapper;

    @Autowired
    private PlanInfoPOConverter converter;

    @Autowired
    private PlanMapper planMapper;

    /**
     * todo 事务 plan 删除处理 硬/软删除
     * {@inheritDoc}
     *
     * @param plan 计划plan
     * @return
     */
    @Override
    public String addPlan(Plan plan) {
        // 判断 plan 是否存在
        PlanPO planPO = planMapper.selectById(plan.getPlanId());
        Verifies.isNull(planPO, "plan is already exist");

        planPO = new PlanPO();
        planPO.setPlanId(plan.getPlanId());
        planPO.setCurrentVersion(plan.getVersion());
        planPO.setRecentlyVersion(plan.getVersion());
        planPO.setIsEnabled(false);
        planPO.setIsDeleted(false);
        planMapper.insert(planPO);

        // 新增 plan info
        PlanInfoPO planInfoPO = converter.convert(plan);
        planInfoPO.setIsDeleted(false);
        planInfoMapper.insert(planInfoPO);

        return plan.getPlanId();
    }

    @Override
    public Integer newPlanVersion(Plan plan) {
        PlanPO planPO = planMapper.selectById(plan.getPlanId());
        Verifies.isNull(planPO, "plan isn't exist");

        Integer newVersion = planPO.getRecentlyVersion() + 1; // 新版本为最大版本 + 1
        plan.setVersion(newVersion);

        int update = planMapper.update(null, Wrappers.<PlanPO>lambdaUpdate()
                .eq(PlanPO::getCurrentVersion, newVersion)
                .eq(PlanPO::getRecentlyVersion, newVersion)
                .eq(PlanPO::getPlanId, planPO.getPlanId())
                .eq(PlanPO::getCurrentVersion, planPO.getCurrentVersion())
                .eq(PlanPO::getRecentlyVersion, planPO.getRecentlyVersion())
        );
        if (update <= 0) {
            // todo 更新失败
            throw new RuntimeException("update fail");
        }

        // 保存基础数据
        PlanInfoPO planInfoPO = converter.convert(plan);
        planInfoPO.setIsDeleted(false);
        planInfoMapper.insert(planInfoPO);
        return newVersion;
    }

    /**
     * {@inheritDoc}
     *
     * @param planId 计划ID
     * @return
     */
    @Override
    public Plan getPlan(String planId, Integer version) {

        PlanInfoPO planInfoPO = planInfoMapper.selectOne(Wrappers.<PlanInfoPO>lambdaQuery()
                .eq(PlanInfoPO::getPlanId, planId)
                .eq(PlanInfoPO::getVersion, version)
        );
        if (planInfoPO == null) {
            return null;
        }
        return converter.reverse().convert(planInfoPO);
    }

    @Override
    public Plan getCurrentPlan(String planId) {
        PlanPO planPO = planMapper.selectById(planId);
        return getPlan(planId, planPO.getCurrentVersion());
    }


    /**
     * TODO 主节点切换 自动查询可调度任务
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<Plan> listSchedulablePlans() {
        throw new UnsupportedOperationException();
    }
}
