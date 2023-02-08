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
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.TaskScheduleTask;
import org.limbo.flowjob.broker.core.schedule.strategy.IPlanScheduleStrategy;
import org.limbo.flowjob.broker.core.schedule.strategy.ITaskResultStrategy;
import org.limbo.flowjob.broker.core.schedule.strategy.ITaskScheduleStrategy;
import org.limbo.flowjob.common.constants.TriggerType;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Devil
 * @since 2023/2/8
 */
@Slf4j
@Component
public class ScheduleStrategy implements IPlanScheduleStrategy, ITaskScheduleStrategy, ITaskResultStrategy {

    @Setter(onMethod_ = @Inject)
    private MetaTaskScheduler metaTaskScheduler;

    @Setter(onMethod_ = @Inject)
    private ScheduleStrategyHelper scheduleStrategyHelper;

    @Override
    public void schedule(TriggerType triggerType, Plan plan, LocalDateTime triggerAt) {
        executeWithAspect(unused -> scheduleStrategyHelper.schedule(triggerType, plan, triggerAt));
    }

    @Override
    public void handleSuccess(Task task, Map<String, Object> context, Object result) {
        executeWithAspect(unused -> scheduleStrategyHelper.handleSuccess(task, context, result));
    }

    @Override
    public void handleFail(Task task, String errorMsg, String errorStackTrace) {
        executeWithAspect(unused -> scheduleStrategyHelper.handleFail(task, errorMsg, errorStackTrace));
    }

    @Override
    public void schedule(Task task) {
        executeWithAspect(unused -> scheduleStrategyHelper.schedule(task));
    }

    public void executeWithAspect(Consumer<Void> consumer) {
        // new context
        newStrategyContext();
        // do real
        consumer.accept(null);
        // do after
        scheduleTasks();
        // clear context
        clearStrategyContext();
    }

    public void scheduleTasks() {
        ScheduleStrategyContext scheduleStrategyContext = getStrategyContext();
        if (scheduleStrategyContext == null || CollectionUtils.isEmpty(scheduleStrategyContext.getRequireScheduleTasks())) {
            return;
        }
        for (TaskScheduleTask task : scheduleStrategyContext.getRequireScheduleTasks()) {
            try {
                metaTaskScheduler.schedule(task);
            } catch (Exception e) {
                // 调度失败 不要影响事务，事务提交后 由task的状态检查任务去修复task的执行情况
                log.error("task schedule fail! task={}", task, e);
            }
        }
    }

    private ScheduleStrategyContext newStrategyContext() {
        ScheduleStrategyContext strategyContext = new ScheduleStrategyContext();
        ScheduleStrategyHelper.STRATEGY_CONTEXT.set(strategyContext);
        return strategyContext;
    }

    private void clearStrategyContext() {
        ScheduleStrategyHelper.STRATEGY_CONTEXT.remove();
    }

    private ScheduleStrategyContext getStrategyContext() {
        return ScheduleStrategyHelper.STRATEGY_CONTEXT.get();
    }

}
