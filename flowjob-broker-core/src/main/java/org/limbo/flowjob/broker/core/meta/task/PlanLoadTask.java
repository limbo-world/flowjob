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
import org.limbo.flowjob.broker.core.meta.info.Plan;
import org.limbo.flowjob.broker.core.meta.info.PlanRepository;
import org.limbo.flowjob.broker.core.meta.processor.PlanInstanceProcessor;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.common.utils.time.Formatters;
import org.limbo.flowjob.common.utils.time.LocalDateTimeUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 获取plan下发
 * 第一次会获取所有的，后续则获取近期更新的
 */
@Slf4j
public class PlanLoadTask {

    private final MetaTaskScheduler scheduler;

    private final PlanRepository planRepository;

    private final PlanInstanceProcessor processor;

    /**
     * 当前节点
     */
    private final Broker broker;

    private final NodeManger nodeManger;

    public PlanLoadTask(MetaTaskScheduler scheduler,
                        PlanRepository planRepository,
                        PlanInstanceProcessor processor,
                        Broker broker,
                        NodeManger nodeManger) {
        this.scheduler = scheduler;
        this.planRepository = planRepository;
        this.processor = processor;
        this.broker = broker;
        this.nodeManger = nodeManger;
    }

    public void init() {
        new Timer().schedule(new InnerTask(), 0, Duration.ofSeconds(1).toMillis());
    }

    private class InnerTask extends TimerTask {

        private LocalDateTime loadTimePoint = LocalDateTimeUtils.parse("2000-01-01 00:00:00", Formatters.YMD_HMS);

        @Override
        public void run() {
            try {
                // 判断自己是否存在 --- 可能由于心跳异常导致不存活
                if (!nodeManger.alive(broker.getRpcBaseURL().toString())) {
                    return;
                }

                // 调度当前时间以及未来的任务
                List<Plan> plans = planRepository.loadUpdatedPlans(broker.getRpcBaseURL(), loadTimePoint.plusSeconds(-1)); // 防止部分延迟导致变更丢失
                loadTimePoint = TimeUtils.currentLocalDateTime();
                if (CollectionUtils.isEmpty(plans)) {
                    return;
                }
                for (Plan plan : plans) {
                    PlanScheduleTask metaTask = new PlanScheduleTask(plan, processor, scheduler);
                    // 移除老的
                    scheduler.unschedule(metaTask.scheduleId());
                    // 调度新的
                    if (TriggerType.SCHEDULE == plan.getTriggerType() && plan.isEnabled()) {
                        scheduler.schedule(metaTask);
                    }
                }
            } catch (Exception e) {
                log.error("[{}] execute fail", this.getClass().getSimpleName(), e);
            }
        }
    }

}
