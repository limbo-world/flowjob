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
import org.limbo.flowjob.common.meta.JobInfo;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanRepository;
import org.limbo.flowjob.broker.core.domain.plan.NormalPlan;
import org.limbo.flowjob.broker.core.domain.plan.WorkflowPlan;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
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
        return assemble(planEntity, planInfoEntity);
    }

    @Override
    public Plan getByVersion(String id, String version) {
        PlanEntity planEntity = planEntityRepo.findById(id).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN + id));
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(version)
                .orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INFO + planEntity.getCurrentVersion()));
        if (!Objects.equals(planInfoEntity.getPlanId(), planEntity.getPlanId())) {
            throw new IllegalArgumentException("plan:" + id + " version:" + version + " not match");
        }
        return assemble(planEntity, planInfoEntity);
    }

    private Plan assemble(PlanEntity planEntity, PlanInfoEntity planInfoEntity) {
        Plan plan;
        PlanType planType = PlanType.parse(planInfoEntity.getPlanType());
        if (PlanType.STANDALONE == planType) {
            plan = new NormalPlan(
                    planInfoEntity.getPlanId(),
                    planInfoEntity.getPlanInfoId(),
                    TriggerType.parse(planInfoEntity.getTriggerType()),
                    DomainConverter.toScheduleOption(planInfoEntity),
                    JacksonUtils.parseObject(planInfoEntity.getJobInfo(), JobInfo.class)
            );
        } else if (PlanType.WORKFLOW == planType) {
            plan = new WorkflowPlan(
                    planInfoEntity.getPlanId(),
                    planInfoEntity.getPlanInfoId(),
                    TriggerType.parse(planInfoEntity.getTriggerType()),
                    DomainConverter.toScheduleOption(planInfoEntity),
                    DomainConverter.toJobDag(planInfoEntity.getJobInfo())
            );
        } else {
            throw new IllegalArgumentException("Illegal PlanType in plan:" + planEntity.getPlanId() + " version:" + planEntity.getCurrentVersion());
        }
        return plan;
    }

}
