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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanInfo;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanRepository;
import org.limbo.flowjob.broker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.common.utils.TimeUtil;

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

    @Setter
    private Scheduler scheduler;

    public BrokerNode(BrokerConfig config, BrokerRegistry registry) {
        this.config = config;
        this.registry = registry;
    }

    public void start() {
        // 节点注册 用于集群感知
        registry.register(config.getHost(), config.getPort());

        // 节点变更通知
        registry.subscribe(event -> {
            switch (event.getType()) {
                case ONLINE:
                    BrokerNodeManger.online(event.getNodeId(), event.getHost(), event.getPort());
                    break;
                case OFFLINE:
                    BrokerNodeManger.offline(event.getHost(), event.getPort());
                    break;
                default:
                    log.warn("[BrokerNodeListener] unknown evnet {}", event);
                    break;
            }
        });


        // 下发任务task
        new Timer("Task-Dispatcher").schedule(new PlanScheduleTask(), 0, config.getRebalanceInterval());

        // 状态检查task
        new Timer("Task-Status-Checker").schedule(new TaskStatusCheckTask(), 0, config.getStatusCheckInterval());

        // 状态检查task
        new Timer("Job-Status-Checker").schedule(new JobStatusCheckTask(), 0, config.getStatusCheckInterval());
    }

    private class PlanScheduleTask extends TimerTask {

        private static final String TASK_NAME = "[PlanScheduleTask]";

        @Override
        public void run() {
            try {
                // 判断自己是否存在 --- 可能由于心跳异常导致不存活
                if (!BrokerNodeManger.alive(config.getHost(), config.getPort())) {
                    return;
                }

                for (;;) {
                    // 调度当前时间以及未来10分钟的任务
                    List<Plan> plans = planRepository.schedulePlans(TimeUtil.currentLocalDateTime().plusMinutes(10));
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
                }
            } catch (Exception e) {
                log.error("{} load and schedule plan fail", TASK_NAME, e);
            }
        }

    }


    private static class TaskStatusCheckTask extends TimerTask {

        @Override
        public void run() {
            // todo 将worker下线的任务改为执行失败
        }
    }

    private static class JobStatusCheckTask extends TimerTask {

        @Override
        public void run() {
            // todo 处理没有生成的job
        }
    }

}
