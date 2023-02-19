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

package org.limbo.flowjob.broker.application.component.schedule;

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

    public void scheduleJob(String planId, String planInstanceId, String jobId) {
        executeWithAspect(unused -> scheduleStrategyHelper.scheduleJob(planId, planInstanceId, jobId));
    }

    @Override
    public void handleSuccess(Task task, Object result) {
        executeWithAspect(unused -> scheduleStrategyHelper.handleSuccess(task, result));
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
        ScheduleStrategyContext.create();
        // do real
        consumer.accept(null);
        // do after
        scheduleTasks();
        // clear context
        ScheduleStrategyContext.clear();
    }

    /**
     * 放在事务外，防止下发和执行很快但是task下发完需要很久的情况，这样前面的任务执行返回后由于事务未提交，会提示找不到task
     */
    public void scheduleTasks() {
        if (CollectionUtils.isEmpty(ScheduleStrategyContext.waitScheduleTasks())) {
            return;
        }
        for (TaskScheduleTask task : ScheduleStrategyContext.waitScheduleTasks()) {
            try {
                metaTaskScheduler.schedule(task);
            } catch (Exception e) {
                // 由task的状态检查任务去修复task的执行情况
                log.error("task schedule fail! task={}", task, e);
            }
        }
    }

}
