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
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.repositories.PlanRepository;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.broker.dao.converter.PlanConverter;
import org.limbo.flowjob.broker.dao.converter.PlanInfoConverter;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;

/**
 * @author Brozen
 * @since 2021-07-13
 */
@Repository
public class PlanRepo implements PlanRepository {

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanConverter planConverter;

    @Setter(onMethod_ = @Inject)
    private PlanInfoConverter planInfoConverter;


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
    public String save(Plan plan) {
        Verifies.notNull(plan.getInfo(), "plan info is null");

        // 新增 Plan
        PlanEntity planEntity = planConverter.toEntity(plan);
        planEntityRepo.saveAndFlush(planEntity);

        // 设置版本
        PlanInfoEntity planInfo = planInfoConverter.toEntity(plan.getInfo());
        planInfo.setPlanId(planEntity.getId());
        planInfoEntityRepo.saveAndFlush(planInfo);

        // 更新版本
        planEntity.setCurrentVersion(planInfo.getId());
        planEntity.setRecentlyVersion(planInfo.getId());
        planEntityRepo.saveAndFlush(planEntity);

        return String.valueOf(planEntity.getId());
    }

    /**
     * {@inheritDoc}
     * @param plan 执行计划领域对象
     * @return newVersion 新版本号
     */
    @Override
    @Transactional
    public String updateVersion(Plan plan) {
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(Long.valueOf(plan.getPlanId()));
        Verifies.verify(planEntityOptional.isPresent(), "plan is not exist");

        // 新增 PlanInfo
        PlanInfoEntity planInfo = planInfoConverter.toEntity(plan.getInfo());
        planInfoEntityRepo.saveAndFlush(planInfo);

        // 更新 Plan 版本信息
        Long newVersion = planInfo.getId();
        PlanEntity planEntity = planEntityOptional.get();
        int effected = planEntityRepo.updateVersion(newVersion, newVersion, planEntity.getId(), planEntity.getCurrentVersion(), planEntity.getRecentlyVersion());
        if (effected > 0) {
            return String.valueOf(newVersion);
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
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(Long.valueOf(planId));
        Verifies.verify(planEntityOptional.isPresent(), "plan is not exist");
        return planConverter.toDO(planEntityOptional.get());
    }


    /**
     * {@inheritDoc}
     * @param plan 作业执行计划
     * @return
     */
    @Override
    public int enablePlan(Plan plan) {
        // update where 乐观锁
        return planEntityRepo.updateEnable(Long.valueOf(plan.getPlanId()), false, true);
    }


    /**
     * {@inheritDoc}
     * @param plan 作业执行计划
     * @return
     */
    @Override
    public int disablePlan(Plan plan) {
        // update where 乐观锁
        return planEntityRepo.updateEnable(Long.valueOf(plan.getPlanId()), true, false);
    }

}
