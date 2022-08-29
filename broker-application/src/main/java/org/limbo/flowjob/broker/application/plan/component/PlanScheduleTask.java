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

package org.limbo.flowjob.broker.application.plan.component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.Node;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanInfo;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.PlanRepository;
import org.limbo.flowjob.broker.dao.domain.SlotManager;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.common.utils.TimeUtil;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * todo 这类task 是否可以直接交由调度系统调度
 */
@Slf4j
@Component
public class PlanScheduleTask extends TimerTask {

    @Setter(onMethod_ = @Inject)
    private BrokerConfig config;

    @Setter(onMethod_ = @Inject)
    private PlanScheduler scheduler;

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    @Setter(onMethod_ = @Inject)
    private NodeManger nodeManger;

    @Setter(onMethod_ = @Inject)
    private PlanSlotEntityRepo planSlotEntityRepo;

    private static final String TASK_NAME = "[PlanScheduleTask]";

    @Override
    public void run() {
        try {
            // 判断自己是否存在 --- 可能由于心跳异常导致不存活
            if (!nodeManger.alive(new Node(config.getHost(), config.getPort()))) {
                return;
            }
            // 调度当前时间以及未来的任务
            List<Plan> plans = schedulePlans(TimeUtil.currentLocalDateTime().plusSeconds(30));
            if (CollectionUtils.isEmpty(plans)) {
                return;
            }

            for (Plan plan : plans) {
                PlanInfo planInfo = plan.getInfo();
                if (planInfo.getScheduleOption().getScheduleType() == null) {
                    log.error("{} scheduleType is null info={}", TASK_NAME, planInfo);
                    continue;
                }
                if (ScheduleType.NONE == planInfo.getScheduleOption().getScheduleType()) {
                    continue;
                }
                PlanInstance planInstance = planInfo.newInstance(TriggerType.SCHEDULE, plan.getNextTriggerAt());
                // 调度
                scheduler.schedule(planInstance);
            }
        } catch (Exception e) {
            log.error("{} load and schedule plan fail", TASK_NAME, e);
        }
    }

    public List<Plan> schedulePlans(LocalDateTime nextTriggerAt) {
        List<Integer> slots = SlotManager.slots(nodeManger.allAlive(), config.getHost(), config.getPort());
        if (CollectionUtils.isEmpty(slots)) {
            return Collections.emptyList();
        }
        List<PlanSlotEntity> slotEntities = planSlotEntityRepo.findBySlotIn(slots);
        if (CollectionUtils.isEmpty(slotEntities)) {
            return Collections.emptyList();
        }
        List<Long> planIds = slotEntities.stream().map(PlanSlotEntity::getPlanId).collect(Collectors.toList());
        List<PlanEntity> planEntities = planEntityRepo.findByIdInAndIsEnabledAndNextTriggerAtBefore(planIds, true, nextTriggerAt);
        if (CollectionUtils.isEmpty(planEntities)) {
            return Collections.emptyList();
        }
        List<Plan> plans = new ArrayList<>();
        for (PlanEntity planEntity : planEntities) {
            plans.add(planRepository.get(planEntity.getId().toString()));
        }
        return plans;
    }


}
