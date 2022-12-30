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
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanInfo;
import org.limbo.flowjob.broker.core.repository.PlanRepository;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.service.IScheduleService;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.broker.dao.support.SlotManager;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
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
    @Setter(onMethod_ = @Inject)
    private JobInfoEntityRepo jobInfoEntityRepo;
    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private IScheduleService iScheduleService; // todo @d 循环依赖
    @Setter(onMethod_ = @Inject)
    private MetaTaskScheduler metaTaskScheduler;

    /**
     * {@inheritDoc}
     *
     * @param plan 计划plan
     * @return
     */
    @Override
    @Transactional
    public String save(Plan plan) {
        Verifies.notNull(plan.getInfo(), "plan info is null id:" + plan.getPlanId());
        Verifies.verify(plan.getInfo().check(), "plan info is invalid id:" + plan.getPlanId());

        PlanInfo planInfo = plan.getInfo();
        // 视图属性 todo 校验dag不为空 且内部节点不能重复

        PlanEntity planEntity = planEntityRepo.findById(plan.getPlanId()).orElse(null);
        PlanInfoEntity planInfoEntity = toEntity(planInfo);
        if (planEntity == null) {
            // 新增 Plan
            planEntity = toEntity(plan);
            planEntity = planEntityRepo.saveAndFlush(planEntity);

            // 槽位保存
            PlanSlotEntity planSlotEntity = new PlanSlotEntity();
            planSlotEntity.setSlot(SlotManager.slot(planEntity.getPlanId()));
            planSlotEntity.setPlanId(planEntity.getPlanId());
            planSlotEntityRepo.saveAndFlush(planSlotEntity);

            // 设置版本
            planInfoEntity.setPlanId(planEntity.getPlanId());
        } else {
            // 更新 Plan 版本信息
            int effected = planEntityRepo.updateVersion(plan.getCurrentVersion(), plan.getRecentlyVersion(), plan.getPlanId(), planEntity.getCurrentVersion(), planEntity.getRecentlyVersion());
            if (effected <= 0) {
                throw new IllegalStateException("更新Plan版本失败");
            }
        }

        // 保存版本信息
        planInfoEntityRepo.saveAndFlush(planInfoEntity);

        // 保存jobInfo信息
        List<JobInfoEntity> jobInfoEntities = new ArrayList<>();
        for (JobInfo jobInfo : planInfo.getDag().nodes()) {
            jobInfoEntities.add(toEntity(planInfoEntity.getPlanInfoId(), jobInfo));
        }
        jobInfoEntityRepo.saveAll(jobInfoEntities);
        jobInfoEntityRepo.flush();

        return plan.getPlanId();
    }

    /**
     * {@inheritDoc}
     *
     * @param planId 计划ID
     * @return
     */
    @Override
    public Plan get(String planId) {
        Optional<PlanEntity> planEntityOptional = planEntityRepo.findById(planId);
        Verifies.verify(planEntityOptional.isPresent(), "plan is not exist " + planId);
        return toPlan(planEntityOptional.get());
    }

    public PlanInfoEntity toEntity(PlanInfo planInfo) {
        PlanInfoEntity entity = new PlanInfoEntity();

        entity.setPlanId(planInfo.getPlanId());
        entity.setPlanVersion(planInfo.getVersion());
        entity.setPlanType(planInfo.getType().status);
        entity.setPlanInfoId(planInfo.getPlanId() + "-" + planInfo.getVersion());

        entity.setDescription(planInfo.getDescription());

        entity.setTriggerType(planInfo.getTriggerType().type);

        ScheduleOption scheduleOption = planInfo.getScheduleOption();
        entity.setScheduleType(scheduleOption.getScheduleType().type);
        entity.setScheduleStartAt(scheduleOption.getScheduleStartAt());
        entity.setScheduleDelay(scheduleOption.getScheduleDelay().toMillis());
        entity.setScheduleInterval(scheduleOption.getScheduleInterval().toMillis());
        entity.setScheduleCron(scheduleOption.getScheduleCron());

        entity.setJobs(planInfo.getDag().json());

        // 能够查询到info信息，说明未删除
        entity.setDeleted(false);

        return entity;
    }

    public PlanEntity toEntity(Plan plan) {
        PlanEntity planEntity = new PlanEntity();
        planEntity.setCurrentVersion(plan.getCurrentVersion());
        planEntity.setRecentlyVersion(plan.getRecentlyVersion());
        planEntity.setEnabled(plan.isEnabled());
        planEntity.setPlanId(plan.getPlanId());
        return planEntity;
    }

    public JobInfoEntity toEntity(String planInfoId, JobInfo jobInfo) {
        JobInfoEntity jobInfoEntity = new JobInfoEntity();
        jobInfoEntity.setPlanInfoId(planInfoId);
        jobInfoEntity.setName(jobInfo.getName());
        jobInfoEntity.setType(jobInfo.getType().type);
        jobInfoEntity.setTriggerType(jobInfo.getTriggerType().type);
        jobInfoEntity.setAttributes(JacksonUtils.toJSONString(jobInfo.getAttributes(), JacksonUtils.DEFAULT_NONE_OBJECT));
        jobInfoEntity.setDispatchOption(JacksonUtils.toJSONString(jobInfo.getDispatchOption(), JacksonUtils.DEFAULT_NONE_OBJECT));
        jobInfoEntity.setExecutorName(jobInfo.getExecutorName());
        jobInfoEntity.setTerminateWithFail(jobInfo.isTerminateWithFail());
        return jobInfoEntity;
    }

    public Plan toPlan(PlanEntity entity) {
        // 获取plan 的当前版本
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findByPlanIdAndPlanVersion(entity.getPlanId(), entity.getCurrentVersion());
        Verifies.notNull(planInfoEntity, "does not find " + entity.getPlanId() + " plan's info by version--" + entity.getCurrentVersion() + "");

        List<JobInfoEntity> jobInfoEntities = jobInfoEntityRepo.findByPlanInfoId(planInfoEntity.getPlanInfoId());
        Verifies.notEmpty(jobInfoEntities, "does not find " + entity.getPlanId() + " plan's job info by version--" + entity.getCurrentVersion() + "");

        PlanInfo planInfo = DomainConverter.toPlanInfo(planInfoEntity, jobInfoEntities);

        // 获取最近一次调度的planInstance和最近一次结束的planInstance
        PlanInstanceEntity latelyTrigger = planInstanceEntityRepo.findLatelyTrigger(entity.getPlanId());
        PlanInstanceEntity latelyFeedback = planInstanceEntityRepo.findLatelyFeedback(entity.getPlanId());

        return new Plan(
                entity.getPlanId(),
                entity.getCurrentVersion(),
                entity.getRecentlyVersion(),
                planInfo,
                entity.isEnabled(),
                latelyTrigger == null ? null : latelyTrigger.getTriggerAt(),
                latelyFeedback == null ? null : latelyFeedback.getFeedbackAt(),
                iScheduleService,
                metaTaskScheduler
        );

    }

}
