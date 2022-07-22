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

package org.limbo.flowjob.broker.core.cluster;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.PlanTriggerType;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanRepository;
import org.limbo.flowjob.broker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.broker.core.utils.TimeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Devil
 * @since 2022/7/20
 */
@Slf4j
public abstract class BrokerNode {

    protected BrokerConfig config;

    protected final BrokerRegistry registry;

    private PlanRepository planRepository;

    private PlanInstanceRepository planInstanceRepository;

    private Scheduler scheduler;

    public BrokerNode(BrokerConfig config, BrokerRegistry registry) {
        this.config = config;
        this.registry = registry;
    }

    public void start() {
        // 节点注册 用于集群感知
        registry.register(config.getHost(), config.getPort());
        // 节点变更通知
        registry.subscribe(new NodeListener() {
            @Override
            public void event(NodeEvent event) {
                switch (event.getType()) {
                    case ONLINE:
                        BrokerNodeManger.online(event.getHost(), event.getPort());
                        break;
                    case OFFLINE:
                        BrokerNodeManger.offline(event.getHost(), event.getPort());
                        break;
                    default:
                        log.warn("[BrokerNodeListener] unknown evnet {}", event);
                        break;
                }
            }
        });


        // 开启定时任务 reload plan
        new Timer().schedule(new PlanScheduleTask(), 0, config.getRebalanceInterval());
    }

    private class PlanScheduleTask extends TimerTask {

        private final String taskName = "[PlanScheduleTask]";

        @Override
        public void run() {
            try {
                // 判断自己是否存在 --- 可能由于心跳异常导致不存活
                if (!BrokerNodeManger.alive(config.getHost(), config.getPort())) {
                    return;
                }
                LocalDateTime now = TimeUtil.nowLocalDateTime();
                List<Plan> plans = planRepository.schedulePlans(now.plusMinutes(-10), now);
                if (CollectionUtils.isEmpty(plans)) {
                    return;
                }

                for (Plan plan : plans) {
                    PlanInfo planInfo = plan.getInfo();
                    if (planInfo.getScheduleOption().getScheduleType() == null) {
                        log.error("{} scheduleType is null info={}", taskName, planInfo);
                        continue;
                    }
                    if (ScheduleType.NONE == planInfo.getScheduleOption().getScheduleType()) {
                        continue;
                    }

                    PlanInstance planInstance = planInstanceRepository.get(plan.getPlanId(), plan.getNextTriggerAt());
                    if (planInstance == null) {
                        planInstance = planInfo.newInstance(PlanScheduleStatus.SCHEDULING, PlanTriggerType.SCHEDULE);
                        String planInstanceId = planInstanceRepository.add(planInstance);
                        if (StringUtils.isBlank(planInstanceId)) {
                            // 并发情况可能导致
                            return;
                        }
                    }
                    // 调度
                    if (!scheduler.isScheduling(planInstance.scheduleId())) {
                        planInstance.schedule();
                    }
                }
            } catch (Exception e) {
                log.error("{} load and schedule plan fail", taskName, e);
            }
        }

    }

}
