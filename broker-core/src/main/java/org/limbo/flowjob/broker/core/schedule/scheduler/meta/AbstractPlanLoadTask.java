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
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.schedule.Scheduled;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 元任务：定时加载 Plan 进行调度
 */
@Slf4j
public abstract class AbstractPlanLoadTask extends FixDelayMetaTask {

    @Getter
    private final BrokerConfig config;

    @Getter
    private final NodeManger nodeManger;

    private final MetaTaskScheduler scheduler;

    protected AbstractPlanLoadTask(Duration interval,
                                   BrokerConfig config,
                                   NodeManger nodeManger,
                                   MetaTaskScheduler scheduler) {
        super(interval, scheduler);
        this.config = config;
        this.nodeManger = nodeManger;
        this.scheduler = scheduler;
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
            List<Plan> plans = loadPlans();
            Map<String, Plan> scheduleMap = plans.stream().collect(Collectors.toMap(MetaTask::scheduleId, plan -> plan));

            // 移除调非本节点的plan
            Collection<MetaTask> metaTasks = scheduler.getSchedulingByType(MetaTaskType.PLAN);
            for (MetaTask metaTask : metaTasks) {
                if (!scheduleMap.containsKey(metaTask.scheduleId())) {
                    scheduler.unschedule(metaTask.scheduleId());
                }
            }

            // 重新调度 新增/版本变更的plan
            if (CollectionUtils.isNotEmpty(plans)) {
                for (Plan plan : plans) {
                    scheduler.schedule(plan);
                }
            }
        } catch (Exception e) {
            log.error("{} load and schedule plan fail", scheduleId(), e);
        }
    }


    /**
     * 加载触发时间在指定时间之前的 Plan。
     */
    protected abstract List<Plan> loadPlans();


    @Override
    public MetaTaskType getType() {
        return MetaTaskType.PLAN_LOAD;
    }

    @Override
    public String getMetaId() {
        return "PlanLoadTask";
    }
}
