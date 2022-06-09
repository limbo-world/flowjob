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
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.repositories.PlanInfoRepository;
import org.limbo.flowjob.broker.core.repositories.PlanRepository;
import org.limbo.flowjob.broker.dao.converter.PlanPOConverter;
import org.limbo.flowjob.broker.dao.mybatis.PlanMapper;
import org.limbo.flowjob.broker.dao.po.PlanPO;
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
    private PlanInfoRepository planInfoRepo;

    @Autowired
    private PlanMapper mapper;

    @Autowired
    private PlanPOConverter converter;


    /**
     * todo 事务 plan 删除处理 硬/软删除
     * TODO 是否存在校验与插入是多个操作，需保证执行前对planId加锁
     * {@inheritDoc}
     *
     * @param plan 计划plan
     * @return
     */
    @Override
    public String addPlan(Plan plan, PlanInfo initialInfo) {
        // 判断 Plan 是否存在，校验 PlanInfo 不为空
        PlanPO po = mapper.selectById(plan.getPlanId());
        Verifies.isNull(po, "plan is already exist");
        Verifies.notNull(initialInfo, "plan info is null");

        // 设置初始版本号
        Integer initialVersion = 1;
        initialInfo.setVersion(initialVersion);
        plan.setCurrentVersion(initialVersion);
        plan.setRecentlyVersion(initialVersion);

        // 新增 Plan
        mapper.insert(converter.convert(plan));

        // 新增 PlanInfo
        planInfoRepo.addVersion(initialInfo);

        return plan.getPlanId();
    }


    /**
     * {@inheritDoc}
     * @param plan 执行计划领域对象
     * @param newVersion 新版本号
     * @return
     */
    @Override
    public Integer updateVersion(Plan plan, Integer newVersion) {
        // 更新 Plan 版本信息
        int effected = mapper.update(null, Wrappers.<PlanPO>lambdaUpdate()
                .set(PlanPO::getCurrentVersion, newVersion)
                .set(PlanPO::getRecentlyVersion, newVersion)
                .eq(PlanPO::getPlanId, plan.getPlanId())
                .eq(PlanPO::getCurrentVersion, plan.getCurrentVersion())
                .eq(PlanPO::getRecentlyVersion, plan.getRecentlyVersion()));

        if (effected > 0) {
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
        PlanPO po = mapper.selectOne(Wrappers
                .<PlanPO>lambdaQuery()
                .eq(PlanPO::getPlanId, planId)
        );
        return converter.reverse().convert(po);
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
        return mapper.update(null, Wrappers
                .<PlanPO>lambdaUpdate()
                .set(PlanPO::getIsEnabled, true)
                .eq(PlanPO::getPlanId, plan.getPlanId())
                .eq(PlanPO::getIsEnabled, false)
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
        return mapper.update(null, Wrappers
                .<PlanPO>lambdaUpdate()
                .set(PlanPO::getIsEnabled, false)
                .eq(PlanPO::getPlanId, plan.getPlanId())
                .eq(PlanPO::getIsEnabled, true)
        );
    }

}
