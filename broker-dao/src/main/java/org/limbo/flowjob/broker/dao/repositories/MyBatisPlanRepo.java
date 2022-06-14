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
import lombok.Setter;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.repositories.PlanRepository;
import org.limbo.flowjob.broker.dao.converter.PlanInfoPOConverter;
import org.limbo.flowjob.broker.dao.converter.PlanPOConverter;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.mybatis.PlanInfoMapper;
import org.limbo.flowjob.broker.dao.mybatis.PlanMapper;
import org.limbo.utils.verifies.Verifies;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Brozen
 * @since 2021-07-13
 */
@Repository
public class MyBatisPlanRepo implements PlanRepository {

    @Setter(onMethod_ = @Inject)
    private PlanMapper planMapper;

    @Setter(onMethod_ = @Inject)
    private PlanInfoMapper planInfoMapper;

    @Setter(onMethod_ = @Inject)
    private PlanPOConverter planPOConverter;

    @Setter(onMethod_ = @Inject)
    private PlanInfoPOConverter planInfoPOConverter;


    /**
     * todo 事务 plan 删除处理 硬/软删除
     * TODO 是否存在校验与插入是多个操作，需保证执行前对planId加锁
     * {@inheritDoc}
     *
     * @param plan 计划plan
     * @return
     */
    @Override
    @Transactional
    public Long addPlan(Plan plan) {
        // 判断 Plan 是否存在，校验 PlanInfo 不为空
        PlanEntity po = planMapper.selectById(plan.getPlanId());
        Verifies.isNull(po, "plan is already exist");
        Verifies.notNull(plan.getInfo(), "plan info is null");

        // 设置初始版本号
        Integer initialVersion = 1;
        plan.getInfo().setVersion(initialVersion);
        plan.setCurrentVersion(initialVersion);
        plan.setRecentlyVersion(initialVersion);

        // 新增 Plan
        planMapper.insert(planPOConverter.toEntity(plan));

        // 新增 PlanInfo
        planInfoMapper.insert(planInfoPOConverter.convert(plan.getInfo()));

        return plan.getPlanId();
    }


    /**
     * {@inheritDoc}
     * @param plan 执行计划领域对象
     * @param newVersion 新版本号
     * @return
     */
    @Override
    @Transactional
    public Integer updateVersion(Plan plan, Integer newVersion) {
        // 新增 PlanInfo
        int infoInserted = planInfoMapper.insert(planInfoPOConverter.convert(plan.getInfo()));

        // 更新 Plan 版本信息
        int effected = planMapper.update(null, Wrappers.<PlanEntity>lambdaUpdate()
                .set(PlanEntity::getCurrentVersion, newVersion)
                .set(PlanEntity::getRecentlyVersion, newVersion)
                .eq(PlanEntity::getPlanId, plan.getPlanId())
                .eq(PlanEntity::getCurrentVersion, plan.getCurrentVersion())
                .eq(PlanEntity::getRecentlyVersion, plan.getRecentlyVersion()));

        if (effected > 0 && infoInserted > 0) {
            return newVersion;
        } else {
            throw new IllegalStateException("更新Plan版本失败");
        }
    }


    /**
     * {@inheritDoc}
     * @param planId 计划ID
     * @return
     */
    @Override
    public Plan get(String planId) {
        PlanEntity po = planMapper.selectOne(Wrappers
                .<PlanEntity>lambdaQuery()
                .eq(PlanEntity::getPlanId, planId)
        );
        return planPOConverter.toDO(po);
    }


    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<Plan> listSchedulablePlans() {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * @param plan 作业执行计划
     * @return
     */
    @Override
    public int enablePlan(Plan plan) {
        // update where 乐观锁
        return planMapper.update(null, Wrappers
                .<PlanEntity>lambdaUpdate()
                .set(PlanEntity::getIsEnabled, true)
                .eq(PlanEntity::getPlanId, plan.getPlanId())
                .eq(PlanEntity::getIsEnabled, false)
        );
    }


    /**
     * {@inheritDoc}
     * @param plan 作业执行计划
     * @return
     */
    @Override
    public int disablePlan(Plan plan) {
        // update where 乐观锁
        return planMapper.update(null, Wrappers
                .<PlanEntity>lambdaUpdate()
                .set(PlanEntity::getIsEnabled, false)
                .eq(PlanEntity::getPlanId, plan.getPlanId())
                .eq(PlanEntity::getIsEnabled, true)
        );
    }

}
