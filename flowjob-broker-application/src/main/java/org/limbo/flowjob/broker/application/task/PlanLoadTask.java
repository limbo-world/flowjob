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

package org.limbo.flowjob.broker.application.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.application.component.SlotManager;
import org.limbo.flowjob.broker.application.converter.MetaTaskConverter;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixDelayMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.api.constants.TriggerType;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 获取plan下发
 * 相比update的任务比较久
 * 此任务主要为防止 plan 调度中异常导致 在时间轮中丢失
 */
@Slf4j
@Component
public class PlanLoadTask extends FixDelayMetaTask {

    private final PlanEntityRepo planEntityRepo;

    private final MetaTaskConverter metaTaskConverter;

    private final SlotManager slotManager;

    private final Broker broker;

    private final NodeManger nodeManger;

    public PlanLoadTask(MetaTaskScheduler scheduler,
                        PlanEntityRepo planEntityRepo,
                        MetaTaskConverter metaTaskConverter,
                        SlotManager slotManager,
                        @Lazy Broker broker,
                        NodeManger nodeManger) {
        super(Duration.ofSeconds(30), scheduler);
        this.planEntityRepo = planEntityRepo;
        this.metaTaskConverter = metaTaskConverter;
        this.slotManager = slotManager;
        this.broker = broker;
        this.nodeManger = nodeManger;
    }

    /**
     * 执行元任务，从 DB 加载一批待调度的 Plan，放到调度器中去。
     */
    @Override
    protected void executeTask() {
        try {
            // 判断自己是否存在 --- 可能由于心跳异常导致不存活
            if (!nodeManger.alive(broker.getName())) {
                return;
            }

            // 调度当前时间以及未来的任务
            List<PlanScheduleTask> plans = loadTasks();
            if (CollectionUtils.isEmpty(plans)) {
                return;
            }
            for (PlanScheduleTask plan : plans) {
                metaTaskScheduler.schedule(plan);
            }
        } catch (Exception e) {
            log.error("{} load and schedule plan task fail", scheduleId(), e);
        }
    }


    /**
     * 加载触发时间在指定时间之前的 Plan。
     */
    private List<PlanScheduleTask> loadTasks() {
        List<String> planIds = slotManager.planIds();
        if (CollectionUtils.isEmpty(planIds)) {
            return Collections.emptyList();
        }
        List<PlanEntity> planEntities = planEntityRepo.loadPlans(planIds);
        if (CollectionUtils.isEmpty(planEntities)) {
            return Collections.emptyList();
        }
        List<PlanScheduleTask> plans = new ArrayList<>();
        for (PlanEntity planEntity : planEntities) {
            plans.add(metaTaskConverter.toPlanScheduleTask(planEntity.getPlanId(), TriggerType.SCHEDULE));
        }
        return plans;
    }


    @Override
    public MetaTaskType getType() {
        return MetaTaskType.PLAN_LOAD;
    }

    @Override
    public String getMetaId() {
        return this.getClass().getSimpleName();
    }

}
