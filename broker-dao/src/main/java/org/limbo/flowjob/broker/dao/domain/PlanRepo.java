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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.BrokerNodeManger;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.repository.PlanRepository;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private BrokerConfig config;

    private static final int SLOT_SIZE = 64;

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
        planEntityRepo.saveAndFlush(planEntity);

        // 槽位保存
        int slot = (int) (planEntity.getId() % SLOT_SIZE);
        planEntity.setSlot(slot);
        planEntityRepo.saveAndFlush(planEntity);

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
        return toPlan(planEntityOptional.get());
    }

    @Override
    public List<Plan> schedulePlans(LocalDateTime nextTriggerAt) {
        List<Integer> slots = slots();
        if (CollectionUtils.isEmpty(slots)) {
            return Collections.emptyList();
        }
        List<PlanEntity> planEntities = planEntityRepo.findBySlotInAndIsEnabledAndNextTriggerAtBefore(slots(), true, nextTriggerAt);
        if (CollectionUtils.isEmpty(planEntities)) {
            return Collections.emptyList();
        }
        List<Plan> plans = new ArrayList<>();
        for (PlanEntity planEntity : planEntities) {
            plans.add(toPlan(planEntity));
        }
        return plans;
    }

    /**
     * 获取槽位的算法
     * @return 当前机器对应的所有槽位
     */
    private List<Integer> slots() {
        List<Pair<String, Integer>> aliveNodes = BrokerNodeManger.alive();
        List<Pair<String, Integer>> sortedNodes = aliveNodes.stream().sorted(Pair::compareTo).collect(Collectors.toList());

        // 判断自己所在的id位置
        int mark = -1;
        for (int i = 0; i < sortedNodes.size(); i++) {
            Pair<String, Integer> node = sortedNodes.get(i);
            if (config.getHost().equals(node.getLeft()) && config.getPort() == node.getRight()) {
                mark = i;
                break;
            }
        }

        if (mark < 0) {
            log.warn("can't find in alive nodes host:{} port:{}", config.getHost(), config.getPort());
            return Collections.emptyList();
        }

        List<Integer> slots = new ArrayList<>();
        while (mark < SLOT_SIZE) {
            slots.add(mark);
            mark += sortedNodes.size();
        }
        log.info("find slots:{}", slots);
        return slots;
    }

    public PlanInfoEntity toEntity(PlanInfo planInfo) {
        PlanInfoEntity entity = new PlanInfoEntity();

        entity.setPlanId(Long.valueOf(planInfo.getPlanId()));
        entity.setDescription(planInfo.getDescription());

        ScheduleOption scheduleOption = planInfo.getScheduleOption();
        entity.setScheduleType(scheduleOption.getScheduleType().type);
        entity.setScheduleStartAt(scheduleOption.getScheduleStartAt());
        entity.setScheduleDelay(scheduleOption.getScheduleDelay().toMillis());
        entity.setScheduleInterval(scheduleOption.getScheduleInterval().toMillis());
        entity.setScheduleCron(scheduleOption.getScheduleCron());
        entity.setJobs(JacksonUtils.toJSONString(planInfo.getDag().nodes()));

        // 能够查询到info信息，说明未删除
        entity.setIsDeleted(false);

        return entity;
    }

    public PlanEntity toEntity(Plan plan) {
        PlanEntity planEntity = new PlanEntity();
        planEntity.setCurrentVersion(Long.valueOf(plan.getCurrentVersion()));
        planEntity.setRecentlyVersion(Long.valueOf(plan.getRecentlyVersion()));
        planEntity.setIsEnabled(plan.isEnabled());
        planEntity.setId(Long.valueOf(plan.getPlanId()));
        return planEntity;
    }

    public Plan toPlan(PlanEntity entity) {
        Plan plan = new Plan();
        plan.setPlanId(String.valueOf(entity.getId()));
        plan.setCurrentVersion(String.valueOf(entity.getCurrentVersion()));
        plan.setRecentlyVersion(String.valueOf(entity.getRecentlyVersion()));
        plan.setEnabled(entity.getIsEnabled());

        // 获取plan 的当前版本
        Optional<PlanInfoEntity> planInfoEntityOptional = planInfoEntityRepo.findById(Long.valueOf(plan.getCurrentVersion()));
        Verifies.verify(planInfoEntityOptional.isPresent(), "does not find info by version--" +plan.getCurrentVersion()+ "");
        PlanInfo currentVersion = DomainConverter.toPlanInfo(planInfoEntityOptional.get());
        plan.setInfo(currentVersion);
        return plan;

    }

}
