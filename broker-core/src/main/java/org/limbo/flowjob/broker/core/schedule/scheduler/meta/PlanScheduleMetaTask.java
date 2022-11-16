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

package org.limbo.flowjob.broker.core.schedule.scheduler.meta;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanInfo;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 元任务：定时加载 Plan 进行调度
 */
@Slf4j
public abstract class PlanScheduleMetaTask extends FixIntervalMetaTask {

    @Getter
    private final BrokerConfig config;

    @Getter
    private final NodeManger nodeManger;

    protected PlanScheduleMetaTask(String taskId, Duration interval, BrokerConfig config, NodeManger nodeManger) {
        super(taskId, interval);
        this.config = config;
        this.nodeManger = nodeManger;
    }


    /**
     * 执行元任务，从 DB 加载一批待调度的 Plan，放到调度器中去。
     */
    @Override
    protected void executeTask() {
        try {
            // 判断自己是否存在 --- 可能由于心跳异常导致不存活
            if (!nodeManger.alive(config.getName())) {
                return;
            }

            // 调度当前时间以及未来的任务
            List<Plan> plans = loadSchedulablePlans(TimeUtils.currentLocalDateTime().plusSeconds(30));
            if (CollectionUtils.isEmpty(plans)) {
                return;
            }

            for (Plan plan : plans) {
                PlanInfo planInfo = plan.getInfo();
                if (planInfo.getScheduleOption().getScheduleType() == null) {
                    log.error("{} scheduleType is null info={}", scheduleId(), planInfo);
                    continue;
                }

                if (ScheduleType.NONE == planInfo.getScheduleOption().getScheduleType()) {
                    continue;
                }

                // 实例化 Plan 并调度
                PlanInstance planInstance = planInfo.newInstance(TriggerType.SCHEDULE, plan.getNextTriggerAt());
                schedulePlan(planInstance);
            }
        } catch (Exception e) {
            log.error("{} load and schedule plan fail", scheduleId(), e);
        }
    }


    /**
     * 加载触发时间在指定时间之前的 Plan。
     *
     * @param nextTriggerAt 指定的触发时间
     */
    protected abstract List<Plan> loadSchedulablePlans(LocalDateTime nextTriggerAt);


    /**
     * 调度 Plan，将 Plan 拆解为 Job 放入调度器。
     */
    protected abstract void schedulePlan(PlanInstance plan);


}
