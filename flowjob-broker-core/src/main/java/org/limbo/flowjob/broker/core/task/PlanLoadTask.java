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

package org.limbo.flowjob.broker.core.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.application.component.BrokerSlotManager;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanRepository;
import org.limbo.flowjob.broker.core.schedule.SchedulerProcessor;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixDelayMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.common.utils.time.DateTimeUtils;
import org.limbo.flowjob.common.utils.time.Formatters;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 获取plan下发
 * 第一次会获取所有的，后续则获取近期更新的
 */
@Slf4j
@Component
// todo
public class PlanLoadTask extends FixDelayMetaTask {

    private final PlanEntityRepo planEntityRepo;

    private final PlanRepository planRepository;

    private final SchedulerProcessor schedulerProcessor;

    private final BrokerSlotManager slotManager;

    private final Broker broker;

    private final NodeManger nodeManger;

    private LocalDateTime loadTimePoint = DateTimeUtils.parse("2000-01-01 00:00:00", Formatters.YMD_HMS);

    public PlanLoadTask(MetaTaskScheduler scheduler,
                        PlanEntityRepo planEntityRepo,
                        PlanRepository planRepository,
                        SchedulerProcessor schedulerProcessor,
                        BrokerSlotManager slotManager,
                        @Lazy Broker broker,
                        NodeManger nodeManger) {
        super(Duration.ofSeconds(1), scheduler);
        this.planEntityRepo = planEntityRepo;
        this.planRepository = planRepository;
        this.schedulerProcessor = schedulerProcessor;
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
            for (PlanScheduleTask metaTask : plans) {
                // 移除老的
                metaTaskScheduler.unschedule(metaTask.scheduleId());
                // todo 过滤掉非调度的 比如配置改为 api下发的
                // 调度新的
                metaTaskScheduler.schedule(metaTask);
            }
        } catch (Exception e) {
            log.error("{} execute fail", scheduleId(), e);
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
        List<PlanEntity> planEntities = planEntityRepo.loadUpdatedPlans(planIds, loadTimePoint.plusSeconds(-1)); // 防止部分延迟导致变更丢失
        loadTimePoint = TimeUtils.currentLocalDateTime();
        if (CollectionUtils.isEmpty(planEntities)) {
            return Collections.emptyList();
        }
        List<PlanScheduleTask> plans = new ArrayList<>();
        for (PlanEntity planEntity : planEntities) {
            Plan plan = planRepository.get(planEntity.getPlanId());
            plans.add(new PlanScheduleTask(plan.getPlanId(), plan.getScheduleOption(), plan.getLatelyTriggerAt(), plan.getLatelyFeedbackAt(), schedulerProcessor, metaTaskScheduler));
        }
        return plans;
    }


    @Override
    public String getType() {
        return "PLAN_LOAD";
    }

    @Override
    public String getMetaId() {
        return this.getClass().getSimpleName();
    }

}
