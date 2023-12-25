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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.core.context.plan.Plan;
import org.limbo.flowjob.broker.core.schedule.SchedulerProcessor;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.LoopMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.common.thread.CommonThreadPool;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 调度PLan的信息创建Instance
 *
 * @author pengqi
 * @date 2023/1/9
 */
@Slf4j
public class PlanScheduleTask extends LoopMetaTask {

    @Getter
    private final Plan plan;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private final SchedulerProcessor schedulerProcessor;

    public PlanScheduleTask(Plan plan, SchedulerProcessor schedulerProcessor, MetaTaskScheduler metaTaskScheduler) {
        super(plan.getLatelyTriggerAt(), plan.getLatelyFeedbackAt(), plan.getScheduleOption(), metaTaskScheduler);
        this.plan = plan;
        this.schedulerProcessor = schedulerProcessor;
    }

    /**
     * plan 的 fixed delay 由于执行代码不在内存中，需要由执行完成的节点触发
     */
    @Override
    protected void executeFixedDelay() {
        executeTask();
        metaTaskScheduler.unschedule(scheduleId());
    }

    @Override
    public void execute() {
        if (plan == null) {
            log.error("{} plan is null", scheduleId());
            return;
        }
        super.execute();
    }

    @Override
    protected void executeTask() {
        try {
            LocalDateTime triggerAt = getLastTriggerAt();
            LocalDateTime scheduleEndAt = getScheduleOption().getScheduleEndAt();
            if (scheduleEndAt != null && scheduleEndAt.isBefore(TimeUtils.currentLocalDateTime())) {
                return;
            }
            CommonThreadPool.IO.submit(() -> schedulerProcessor.schedule(plan, TriggerType.SCHEDULE, new Attributes(), triggerAt));
        } catch (Exception e) {
            log.error("{} execute fail", scheduleId(), e);
        }
    }

    @Override
    public LocalDateTime calNextTriggerAt() {
        LocalDateTime triggerAt = super.calNextTriggerAt();
        return triggerAt.truncatedTo(ChronoUnit.SECONDS);  // 这里获取到的是毫秒 转为秒
    }

    @Override
    public String getType() {
        return "PLAN";
    }

    @Override
    public String getMetaId() {
        return plan.getId();
    }
}
