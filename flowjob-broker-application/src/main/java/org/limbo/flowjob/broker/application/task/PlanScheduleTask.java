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

package org.limbo.flowjob.broker.application.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.constants.ScheduleType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.application.schedule.ScheduleProxy;
import org.limbo.flowjob.broker.application.support.CommonThreadPool;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.LoopMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;
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
    @JsonIgnore
    private final Plan plan;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private final ScheduleProxy scheduleProxy;

    public PlanScheduleTask(Plan plan, LocalDateTime lastTriggerAt, LocalDateTime lastFeedbackAt,
                            ScheduleProxy scheduleProxy, MetaTaskScheduler metaTaskScheduler) {
        super(lastTriggerAt, lastFeedbackAt, plan.getScheduleOption(), metaTaskScheduler);
        this.plan = plan;
        this.scheduleProxy = scheduleProxy;
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
        if (TriggerType.SCHEDULE != plan.getTriggerType()) {
            return;
        }
        super.execute();
    }

    @Override
    protected void executeTask() {
        try {
            LocalDateTime triggerAt;
            if (ScheduleType.FIXED_DELAY == getScheduleOption().getScheduleType()) {
                triggerAt = getNextTriggerAt();
            } else {
                triggerAt = getLastTriggerAt();
            }
            LocalDateTime scheduleEndAt = plan.getScheduleOption().getScheduleEndAt();
            if (scheduleEndAt != null && scheduleEndAt.isBefore(TimeUtils.currentLocalDateTime())) {
                return;
            }
            CommonThreadPool.IO.submit(() -> scheduleProxy.schedule(TriggerType.SCHEDULE, plan, null, triggerAt));
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
    public MetaTaskType getType() {
        return MetaTaskType.PLAN;
    }

    @Override
    public String getMetaId() {
        return plan.getPlanId() + "-" + plan.getVersion();
    }
}
