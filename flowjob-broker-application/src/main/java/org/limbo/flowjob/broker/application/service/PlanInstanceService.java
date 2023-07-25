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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.PlanStatus;
import org.limbo.flowjob.api.constants.ScheduleType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.console.PlanInstanceDTO;
import org.limbo.flowjob.api.param.console.PlanInstanceQueryParam;
import org.limbo.flowjob.broker.application.component.SlotManager;
import org.limbo.flowjob.broker.application.support.JpaHelper;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/4/14
 */
@Slf4j
@Service
public class PlanInstanceService {

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

    @Transactional
    public void save(String planInstanceId, TriggerType triggerType, Plan plan, LocalDateTime triggerAt) {
        String planId = plan.getPlanId();
        String version = plan.getVersion();

        PlanEntity planEntity = planEntityRepo.selectForUpdate(planId);
        Verifies.notNull(planEntity, MsgConstants.CANT_FIND_PLAN + planId);
        Verifies.verify(!planEntity.isDeleted(), "plan:" + planId + " is deleted!");
        Verifies.verify(planEntity.isEnabled(), "plan:" + planId + " is not enabled!");
        // 判断任务配置信息是否变动：任务是由之前时间创建的 调度时候如果版本改变 可能会有调度时间的变化本次就无需执行
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
        planInstanceEntity.setPlanInstanceId(planInstanceId);
        planInstanceEntity.setPlanId(planId);
        planInstanceEntity.setPlanInfoId(version);
        planInstanceEntity.setStatus(PlanStatus.SCHEDULING.status);
        planInstanceEntity.setTriggerType(triggerType.type);
        planInstanceEntity.setScheduleType(planInfoEntity.getScheduleType());
        planInstanceEntity.setTriggerAt(triggerAt);
        planInstanceEntityRepo.saveAndFlush(planInstanceEntity);
    }

    public PageDTO<PlanInstanceDTO> page(PlanInstanceQueryParam param) {
        Specification<PlanInstanceEntity> sf = (root, query, cb) -> {
            //用于添加所有查询条件
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("planId").as(String.class), param.getPlanId()));
            if (StringUtils.isNotBlank(param.getTriggerAtBegin()) && StringUtils.isNotBlank(param.getTriggerAtEnd())) {
                p.add(cb.greaterThanOrEqualTo(root.get("triggerAt").as(String.class), param.getTriggerAtBegin()));
                p.add(cb.lessThanOrEqualTo(root.get("triggerAt").as(String.class), param.getTriggerAtEnd()));
            }
            Predicate[] pre = new Predicate[p.size()];
            Predicate and = cb.and(p.toArray(pre));
            query.where(and);

            //设置排序
            List<Order> orders = new ArrayList<>();
            orders.add(cb.desc(root.get("triggerAt")));
            return query.orderBy(orders).getRestriction();
        };
        Pageable pageable = JpaHelper.pageable(param);
        Page<PlanInstanceEntity> queryResult = planInstanceEntityRepo.findAll(sf, pageable);
        List<PlanInstanceEntity> planInstanceEntities = queryResult.getContent();
        PageDTO<PlanInstanceDTO> page = PageDTO.convertByPage(param);
        page.setTotal(queryResult.getTotalElements());
        if (CollectionUtils.isNotEmpty(planInstanceEntities)) {
            page.setData(planInstanceEntities.stream().map(planInstanceEntity -> {
                PlanInstanceDTO dto = new PlanInstanceDTO();
                dto.setPlanInstanceId(planInstanceEntity.getPlanInstanceId());
                dto.setPlanId(planInstanceEntity.getPlanId());
                dto.setPlanInfoId(planInstanceEntity.getPlanInfoId());
                dto.setStatus(planInstanceEntity.getStatus());
                dto.setTriggerType(planInstanceEntity.getTriggerType());
                dto.setScheduleType(planInstanceEntity.getScheduleType());
                dto.setTriggerAt(planInstanceEntity.getTriggerAt());
                dto.setStartAt(planInstanceEntity.getStartAt());
                dto.setFeedbackAt(planInstanceEntity.getFeedbackAt());
                return dto;
            }).collect(Collectors.toList()));
        }
        return page;
    }

}
