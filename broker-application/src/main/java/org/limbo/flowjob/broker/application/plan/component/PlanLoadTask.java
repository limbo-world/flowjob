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
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanFactory;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.PlanRepository;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.PlanLoadMetaTask;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.broker.dao.support.SlotManager;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 获取可以下发的plan 创建对应的 PlanInstance 进行调度
 */
@Slf4j
public class PlanLoadTask extends PlanLoadMetaTask {

    @Setter(onMethod_ = @Inject)
    private PlanScheduler scheduler;

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    @Setter(onMethod_ = @Inject)
    private PlanSlotEntityRepo planSlotEntityRepo;


    public PlanLoadTask(Duration interval, BrokerConfig config, NodeManger nodeManger, PlanFactory planFactory) {
        super("Meta[PlanScheduleTask]", interval, config, nodeManger, planFactory);
    }


    /**
     * {@inheritDoc}
     * @param nextTriggerAt 指定的触发时间
     * @return
     */
    protected List<Plan> loadSchedulablePlans(LocalDateTime nextTriggerAt) {
        List<Integer> slots = SlotManager.slots(getNodeManger().allAlive(), getConfig().getHost(), getConfig().getPort());
        if (CollectionUtils.isEmpty(slots)) {
            return Collections.emptyList();
        }
        List<PlanSlotEntity> slotEntities = planSlotEntityRepo.findBySlotIn(slots);
        if (CollectionUtils.isEmpty(slotEntities)) {
            return Collections.emptyList();
        }
        List<String> planIds = slotEntities.stream().map(PlanSlotEntity::getPlanId).collect(Collectors.toList());
        // todo 这里可以只获取id
        List<PlanEntity> planEntities = planEntityRepo.findByPlanIdInAndEnabledAndNextTriggerAtBefore(planIds, true, nextTriggerAt);
        if (CollectionUtils.isEmpty(planEntities)) {
            return Collections.emptyList();
        }
        List<Plan> plans = new ArrayList<>();
        for (PlanEntity planEntity : planEntities) {
            plans.add(planRepository.get(planEntity.getPlanId()));
        }
        return plans;
    }


    /**
     * {@inheritDoc}
     * @param plan
     */
    @Override
    protected void schedulePlan(PlanInstance plan) {
        scheduler.schedule(plan);
    }

}
