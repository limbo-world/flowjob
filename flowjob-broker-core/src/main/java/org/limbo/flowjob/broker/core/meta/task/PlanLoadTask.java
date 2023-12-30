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

package org.limbo.flowjob.broker.core.meta.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.meta.plan.Plan;
import org.limbo.flowjob.broker.core.meta.plan.PlanRepository;
import org.limbo.flowjob.broker.core.schedule.SchedulerProcessor;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixDelayMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.common.utils.time.DateTimeUtils;
import org.limbo.flowjob.common.utils.time.Formatters;
import org.limbo.flowjob.common.utils.time.TimeUtils;

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
public class PlanLoadTask extends FixDelayMetaTask {

    private final PlanRepository planRepository;

    private final SchedulerProcessor schedulerProcessor;

    /**
     * 当前节点
     */
    private final Broker broker;

    private final NodeManger nodeManger;

    private LocalDateTime loadTimePoint = DateTimeUtils.parse("2000-01-01 00:00:00", Formatters.YMD_HMS);

    public PlanLoadTask(MetaTaskScheduler scheduler,
                        PlanRepository planRepository,
                        SchedulerProcessor schedulerProcessor,
                        Broker broker,
                        NodeManger nodeManger) {
        super(Duration.ofSeconds(1), scheduler);
        this.planRepository = planRepository;
        this.schedulerProcessor = schedulerProcessor;
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
            if (!nodeManger.alive(broker.getRpcBaseURL().toString())) {
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
        List<Plan> plans = planRepository.loadUpdatedPlans(broker.getRpcBaseURL(), loadTimePoint.plusSeconds(-1)); // 防止部分延迟导致变更丢失
        loadTimePoint = TimeUtils.currentLocalDateTime();
        if (CollectionUtils.isEmpty(plans)) {
            return Collections.emptyList();
        }
        List<PlanScheduleTask> planScheduleTasks = new ArrayList<>();
        for (Plan plan : plans) {
            if (TriggerType.SCHEDULE == plan.getTriggerType()) {
                planScheduleTasks.add(new PlanScheduleTask(plan, schedulerProcessor, metaTaskScheduler));
            }
        }
        return planScheduleTasks;
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
