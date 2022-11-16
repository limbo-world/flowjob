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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanInfo;
import org.limbo.flowjob.broker.core.repository.PlanRepository;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.broker.dao.support.DBFieldHelper;
import org.limbo.flowjob.broker.dao.support.SlotManager;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;

/**
 * @author Brozen
 * @since 2021-07-13
 */
@Slf4j
@Repository
public class PlanRepo implements PlanRepository {

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;
    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;
    @Setter(onMethod_ = @Inject)
    private PlanSlotEntityRepo planSlotEntityRepo;

    /**
     * {@inheritDoc}
     *
     * @param plan 计划plan
     * @return
     */
    @Override
    @Transactional
    public String save(Plan plan) {
        Verifies.notNull(plan.getInfo(), "plan info is null");
        Verifies.verify(plan.getInfo().check(), "plan info has error");

        if (StringUtils.isBlank(plan.getPlanId())) {
            return add(plan);
        } else {
            return replace(plan);
        }
    }

    private String add(Plan plan) {
        // 新增 Plan
        PlanEntity planEntity = toEntity(plan);
        planEntity = planEntityRepo.saveAndFlush(planEntity);

        // 槽位保存
        PlanSlotEntity planSlotEntity = new PlanSlotEntity();
        planSlotEntity.setSlot(SlotManager.slot(planEntity.getId()));
        planSlotEntity.setPlanId(planEntity.getId());
        planSlotEntityRepo.saveAndFlush(planSlotEntity);

        // 设置版本
        PlanInfoEntity planInfo = toEntity(plan.getInfo());
        planInfo.setPlanId(planEntity.getId());
        planInfoEntityRepo.saveAndFlush(planInfo);

        // 更新版本
        planEntity.setCurrentVersion(planInfo.getId());
        planEntity.setRecentlyVersion(planInfo.getId());
        planEntityRepo.saveAndFlush(planEntity);

        return String.valueOf(planEntity.getId());
    }

    private String replace(Plan plan) {
        // 新增 PlanInfo
        PlanInfoEntity planInfo = toEntity(plan.getInfo());
        planInfoEntityRepo.saveAndFlush(planInfo);

        // 更新 Plan 版本信息
        Long newVersion = planInfo.getId();
        int effected = planEntityRepo.updateVersion(newVersion, newVersion, Long.valueOf(plan.getPlanId()), Long.valueOf(plan.getCurrentVersion()), Long.valueOf(plan.getRecentlyVersion()));
        if (effected > 0) {
            return String.valueOf(plan.getPlanId());
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
        Verifies.verify(planEntityOptional.isPresent(), "plan is not exist " + planId);
        return toPlan(planEntityOptional.get());
    }

    public PlanInfoEntity toEntity(PlanInfo planInfo) {
        PlanInfoEntity entity = new PlanInfoEntity();

        entity.setPlanId(NumberUtils.toLong(planInfo.getPlanId()));
        entity.setDescription(planInfo.getDescription());

        ScheduleOption scheduleOption = planInfo.getScheduleOption();
        entity.setScheduleType(scheduleOption.getScheduleType().type);
        entity.setTriggerType(scheduleOption.getTriggerType().type);
        entity.setScheduleStartAt(scheduleOption.getScheduleStartAt());
        entity.setScheduleDelay(scheduleOption.getScheduleDelay().toMillis());
        entity.setScheduleInterval(scheduleOption.getScheduleInterval().toMillis());
        entity.setScheduleCron(scheduleOption.getScheduleCron());
        entity.setJobs(JacksonUtils.toJSONString(planInfo.getDag().nodes()));

        // 能够查询到info信息，说明未删除
        entity.setIsDeleted(DBFieldHelper.FALSE_LONG);

        return entity;
    }

    public PlanEntity toEntity(Plan plan) {
        PlanEntity planEntity = new PlanEntity();
        planEntity.setCurrentVersion(NumberUtils.toLong(plan.getCurrentVersion()));
        planEntity.setRecentlyVersion(NumberUtils.toLong(plan.getRecentlyVersion()));
        planEntity.setIsEnabled(DBFieldHelper.boolToLong(plan.isEnabled(), StringUtils.isBlank(plan.getPlanId()) ? null : Long.valueOf(plan.getPlanId())));
        planEntity.setId(NumberUtils.toLong(plan.getPlanId()));
        planEntity.setNextTriggerAt(plan.getNextTriggerAt());
        return planEntity;
    }

    public Plan toPlan(PlanEntity entity) {
        Plan plan = new Plan();
        plan.setPlanId(String.valueOf(entity.getId()));
        plan.setCurrentVersion(String.valueOf(entity.getCurrentVersion()));
        plan.setRecentlyVersion(String.valueOf(entity.getRecentlyVersion()));
        plan.setEnabled(entity.isEnabled());
        plan.setNextTriggerAt(entity.getNextTriggerAt());

        // 获取plan 的当前版本
        Optional<PlanInfoEntity> planInfoEntityOptional = planInfoEntityRepo.findById(Long.valueOf(plan.getCurrentVersion()));
        Verifies.verify(planInfoEntityOptional.isPresent(), "does not find info by version--" +plan.getCurrentVersion()+ "");
        PlanInfo currentVersion = DomainConverter.toPlanInfo(planInfoEntityOptional.get());
        plan.setInfo(currentVersion);
        return plan;

    }

}
