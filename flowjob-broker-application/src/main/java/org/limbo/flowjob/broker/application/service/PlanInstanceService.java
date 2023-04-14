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

package org.limbo.flowjob.broker.application.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.application.component.SlotManager;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstanceRepository;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.PlanStatus;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.exception.VerifyException;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author Devil
 * @since 2023/4/14
 */
@Slf4j
@Service
public class PlanInstanceService implements PlanInstanceRepository {

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private SlotManager slotManager;

    @Setter(onMethod_ = @Inject)
    private PlanSlotEntityRepo planSlotEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Override
    @Transactional
    public void save(PlanInstance planInstance) {
        Plan plan = planInstance.getPlan();
        String planId = plan.getPlanId();
        String version = plan.getVersion();
        LocalDateTime triggerAt = planInstance.getTriggerAt();
        TriggerType triggerType = planInstance.getTriggerType();

        PlanEntity planEntity = planEntityRepo.selectForUpdate(planId);
        Verifies.notNull(planEntity, MsgConstants.CANT_FIND_PLAN + planId);
        Verifies.verify(!planEntity.isDeleted(), "plan:" + planId + " is deleted!");
        Verifies.verify(planEntity.isEnabled(), "plan:" + planId + " is not enabled!");
        // 任务是由之前时间创建的 调度时候如果版本改变 可能会有调度时间的变化本次就无需执行
        // 比如 5s 执行一次 分别在 5s 10s 15s 在11s的时候内存里下次执行为 15s 此时修改为 2s 执行一次 那么重新加载plan后应该为 12s 14s 所以15s这次可以跳过
        Verifies.verify(Objects.equals(version, planEntity.getCurrentVersion()), MessageFormat.format("plan:{0} version {1} change to {2}", planId, version, planEntity.getCurrentVersion()));

        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(version).orElseThrow(VerifyException.supplier(MessageFormat.format("does not find {0} plan info by version {1}", planId, version)));

        // 判断是否由当前节点执行
        if (TriggerType.API != triggerType) {

            Verifies.verify(planEntity.isEnabled(), "plan " + planId + " is not enabled");

            List<Integer> slots = slotManager.slots();
            Verifies.notEmpty(slots, "slots is empty");
            PlanSlotEntity planSlotEntity = planSlotEntityRepo.findByPlanId(planId);
            Verifies.notNull(planSlotEntity, "plan's slot is null id:" + planId);
            Verifies.verify(slots.contains(planSlotEntity.getSlot()), MessageFormat.format("plan {0} is not in this broker", planId));

            // 校验是否重复创建
            PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findLatelyTrigger(planId, planInfoEntity.getPlanInfoId(), planInfoEntity.getScheduleType(), triggerType.type);
            ScheduleType scheduleType = ScheduleType.parse(planInfoEntity.getScheduleType());
            switch (scheduleType) {
                case FIXED_RATE:
                case CRON:
                    Verifies.verify(planInstanceEntity == null || !triggerAt.isEqual(planInstanceEntity.getTriggerAt()),
                            MessageFormat.format("Duplicate create PlanInstance,triggerAt:{0} planId[{1}] Version[{2}] oldPlanInstance[{3}]",
                                    triggerAt, planId, version, JacksonUtils.toJSONString(planInstanceEntity))
                    );
                    break;
                case FIXED_DELAY:
                    Verifies.verify(planInstanceEntity == null || (!triggerAt.isEqual(planInstanceEntity.getTriggerAt()) && PlanStatus.parse(planInstanceEntity.getStatus()).isCompleted()),
                            MessageFormat.format("Please wait last PlanInstance[{0}] complete.Plan[{1}] Version[{2}]",
                                    JacksonUtils.toJSONString(planInstanceEntity), planId, version)
                    );
                    break;
                default:
                    throw new VerifyException(MsgConstants.UNKNOWN + " scheduleType " + planInfoEntity.getScheduleType());
            }
        }


        PlanInstanceEntity planInstanceEntity = new PlanInstanceEntity();
        planInstanceEntity.setPlanInstanceId(planInstance.getPlanInstanceId());
        planInstanceEntity.setPlanId(planId);
        planInstanceEntity.setPlanInfoId(version);
        planInstanceEntity.setStatus(PlanStatus.SCHEDULING.status);
        planInstanceEntity.setTriggerType(triggerType.type);
        planInstanceEntity.setScheduleType(planInfoEntity.getScheduleType());
        planInstanceEntity.setTriggerAt(triggerAt);
        planInstanceEntityRepo.saveAndFlush(planInstanceEntity);
    }
}
