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

package org.limbo.flowjob.broker.application.component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.application.component.schedule.ScheduleStrategy;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixDelayMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.constants.PlanStatus;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

/**
 * 获取可以下发的plan 创建对应的 PlanInstance 进行调度
 */
@Slf4j
@Component
public class PlanInstanceExecuteCheckTask extends FixDelayMetaTask {

    @Setter(onMethod_ = @Inject)
    private Broker broker;

    @Setter(onMethod_ = @Inject)
    private NodeManger nodeManger;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private ScheduleStrategy scheduleStrategy;

    @Setter(onMethod_ = @Inject)
    private SlotManager slotManager;

    private static final long INTERVAL = 30;

    public PlanInstanceExecuteCheckTask(MetaTaskScheduler scheduler) {
        super(Duration.ofSeconds(INTERVAL), scheduler);
    }

    @Override
    protected void executeTask() {
        // 判断自己是否存在 --- 可能由于心跳异常导致不存活
        if (!nodeManger.alive(broker.getName())) {
            return;
        }

        List<String> planIds = slotManager.planIds();
        if (CollectionUtils.isEmpty(planIds)) {
            return;
        }

        // 一段时候后还是 还是 SCHEDULING 状态的，需要重新调度
        List<PlanInstanceEntity> list = planInstanceEntityRepo.findByPlanIdInAndTriggerAtLessThanEqualAndStatus(planIds, TimeUtils.currentLocalDateTime().plusSeconds(-INTERVAL), PlanStatus.SCHEDULING.status);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (PlanInstanceEntity entity : list) {
            scheduleStrategy.schedulePlanInstance(entity.getPlanId(), entity.getPlanInstanceId(), entity.getTriggerAt());
        }
    }

    @Override
    public MetaTaskType getType() {
        return MetaTaskType.PLAN_EXECUTE_CHECK;
    }

    @Override
    public String getMetaId() {
        return "PlanInstanceExecuteCheckTask";
    }

}
