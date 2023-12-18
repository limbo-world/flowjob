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
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanRepository;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Devil
 * @since 2023/5/8
 */
@Repository
public class PlanRepo implements PlanRepository {

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Override
    public Plan get(String id) {
        PlanEntity planEntity = planEntityRepo.findById(id).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN + id));
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(planEntity.getCurrentVersion())
                .orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INFO + planEntity.getCurrentVersion()));
        return assemble(planInfoEntity);
    }

    @Override
    @Transactional
    public Plan lockAndGet(String id) {
        PlanEntity planEntity = planEntityRepo.selectForUpdate(id);
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(planEntity.getCurrentVersion())
                .orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INFO + planEntity.getCurrentVersion()));
        return assemble(planInfoEntity);
    }

    @Override
    public Plan getByVersion(String id, String version) {
        PlanEntity planEntity = planEntityRepo.findById(id).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN + id));
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(version)
                .orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INFO + planEntity.getCurrentVersion()));
        if (!Objects.equals(planInfoEntity.getPlanId(), planEntity.getPlanId())) {
            throw new IllegalArgumentException("plan:" + id + " version:" + version + " not match");
        }
        return assemble(planInfoEntity);
    }

    @Override
    public List<Plan> loadUpdatedPlans() {
        // todo
        return null;
    }

    private Plan assemble(PlanInfoEntity planInfoEntity) {
        PlanType planType = PlanType.parse(planInfoEntity.getPlanType());

        // 获取最近一次调度的planInstance和最近一次结束的planInstance
        ScheduleOption scheduleOption = plan.getScheduleOption();

        PlanInstanceEntity latelyTrigger = planInstanceEntityRepo.findLatelyTrigger(planId, plan.getVersion(), scheduleOption.getScheduleType().type, triggerType.type);
        PlanInstanceEntity latelyFeedback = planInstanceEntityRepo.findLatelyFeedback(planId, plan.getVersion(), scheduleOption.getScheduleType().type, triggerType.type);

        LocalDateTime latelyTriggerAt = latelyTrigger == null || latelyTrigger.getTriggerAt() == null ? null : latelyTrigger.getTriggerAt().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime latelyFeedbackAt = latelyFeedback == null || latelyFeedback.getFeedbackAt() == null ? null : latelyFeedback.getFeedbackAt().truncatedTo(ChronoUnit.SECONDS);

        if (PlanType.STANDALONE == planType) {
            WorkflowJobInfo jobInfo = JacksonUtils.parseObject(planInfoEntity.getJobInfo(), WorkflowJobInfo.class);
            return new Plan(
                    planInfoEntity.getPlanId(),
                    planInfoEntity.getPlanInfoId(),
                    planType,
                    TriggerType.parse(planInfoEntity.getTriggerType()),
                    DomainConverter.toScheduleOption(planInfoEntity),
                    new DAG<>(Collections.singletonList(jobInfo)),
                    latelyTriggerAt,
                    latelyFeedbackAt
            );
        } else {
            return new Plan(
                    planInfoEntity.getPlanId(),
                    planInfoEntity.getPlanInfoId(),
                    planType,
                    TriggerType.parse(planInfoEntity.getTriggerType()),
                    DomainConverter.toScheduleOption(planInfoEntity),
                    DomainConverter.toJobDag(planInfoEntity.getJobInfo()),
                    latelyTriggerAt,
                    latelyFeedbackAt
            );
        }
    }

}
