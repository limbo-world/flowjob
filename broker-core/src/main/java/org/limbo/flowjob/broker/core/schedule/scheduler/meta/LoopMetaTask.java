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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.broker.core.schedule.Calculated;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.LocalDateTime;

/**
 * 不断循环执行的任务
 * 目前不兼容
 *
 * @author Devil
 * @since 2022/12/19
 */
@Getter
public abstract class LoopMetaTask implements MetaTask, Calculated {

    /**
     * 下次任务触发时间
     */
    private LocalDateTime triggerAt;

    /**
     * 上次被调度时间
     */
    private LocalDateTime lastTriggerAt;

    /**
     * 上次调度反馈的时间
     */
    private LocalDateTime lastFeedbackAt;

    private final ScheduleOption scheduleOption;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private ScheduleCalculator scheduleCalculator;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private MetaTaskScheduler metaTaskScheduler;

    protected LoopMetaTask(LocalDateTime triggerAt, ScheduleOption scheduleOption, MetaTaskScheduler metaTaskScheduler) {
        this.triggerAt = triggerAt;
        this.scheduleOption = scheduleOption;
        this.metaTaskScheduler = metaTaskScheduler;
    }

    /**
     * 触发元任务执行，并更新元任务的触发时间。
     */
    @Override
    public void execute() {
        try {
            executeTask();

            lastTriggerAt = getTriggerAt();
            triggerAt = nextTriggerAt();
            lastFeedbackAt = TimeUtils.currentLocalDateTime();
        } finally {
            metaTaskScheduler.unschedule(scheduleId());
            metaTaskScheduler.schedule(this);
        }
    }

    /**
     * 具体执行逻辑
     */
    protected abstract void executeTask();

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public LocalDateTime triggerAt() {
        return triggerAt;
    }

    @Override
    public ScheduleOption scheduleOption() {
        return scheduleOption;
    }

    @Override
    public LocalDateTime lastTriggerAt() {
        return lastTriggerAt;
    }

    /**
     * 下次触发时间
     */
    public LocalDateTime nextTriggerAt() {
        return TimeUtils.toLocalDateTime(lazyInitTriggerCalculator().calculate(this));
    }

    @Override
    public LocalDateTime lastFeedbackAt() {
        return lastFeedbackAt;
    }

    /**
     * 延迟加载作业触发计算器
     */
    private ScheduleCalculator lazyInitTriggerCalculator() {
        if (scheduleCalculator == null) {
            scheduleCalculator = ScheduleCalculatorFactory.create(scheduleOption.getScheduleType());
        }

        return scheduleCalculator;
    }
}
