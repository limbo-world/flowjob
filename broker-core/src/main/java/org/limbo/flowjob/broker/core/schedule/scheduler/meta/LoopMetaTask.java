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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Getter
public abstract class LoopMetaTask implements MetaTask, Calculated {

    /**
     * 上次任务触发时间
     */
    private LocalDateTime lastTriggerAt;

    /**
     * 当前任务触发时间
     */
    private LocalDateTime triggerAt;

    /**
     * 下次任务触发时间
     */
    private LocalDateTime nextTriggerAt;

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

    protected LoopMetaTask(LocalDateTime lastTriggerAt, LocalDateTime lastFeedbackAt, ScheduleOption scheduleOption, MetaTaskScheduler metaTaskScheduler) {
        this.lastTriggerAt = lastTriggerAt;
        this.lastFeedbackAt = lastFeedbackAt;
        this.scheduleOption = scheduleOption;
        this.metaTaskScheduler = metaTaskScheduler;
        this.nextTriggerAt = calNextTriggerAt();
    }

    /**
     * 触发元任务执行，并更新元任务的触发时间。
     */
    @Override
    public void execute() {
        switch (scheduleOption.getScheduleType()) {
            case FIXED_RATE:
            case CRON:
                executeFixedRate();
                break;
            case FIXED_DELAY:
                executeFixedDelay();
                break;
            default:
                break;
        }
    }

    protected void executeFixedRate() {
        try {
            lastTriggerAt = triggerAt;
            triggerAt = nextTriggerAt;
            nextTriggerAt = calNextTriggerAt();
            metaTaskScheduler.schedule(this);
            executeTask();
        } finally {
            lastFeedbackAt = TimeUtils.currentLocalDateTime();
        }
    }

    protected void executeFixedDelay() {
        try {
            lastTriggerAt = triggerAt;
            triggerAt = nextTriggerAt;
            executeTask();
        } finally {
            lastFeedbackAt = TimeUtils.currentLocalDateTime();
            nextTriggerAt = calNextTriggerAt();
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
    public LocalDateTime scheduleAt() {
        return nextTriggerAt;
    }

    @Override
    public ScheduleOption scheduleOption() {
        return scheduleOption;
    }

    @Override
    public LocalDateTime lastTriggerAt() {
        // 只有 fixRate会被用到 对于本模型来说 就是当前触发时间
        return triggerAt;
    }

    /**
     * 下次触发时间
     */
    public LocalDateTime calNextTriggerAt() {
        Long calculate = lazyInitTriggerCalculator().calculate(this); // 这里获取到的是毫秒 转为秒
        return TimeUtils.toLocalDateTime(calculate);
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

    @Override
    public String scheduleId() {
        return MetaTask.super.scheduleId() + "-" + scheduleAt();
    }
}
