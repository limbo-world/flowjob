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

package org.limbo.flowjob.broker.core.plan;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventPublisher;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.schedule.Schedulable;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.broker.core.utils.TimeUtil;

import javax.inject.Inject;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 可调度的plan 执行调度逻辑
 *
 * @author Devil
 * @since 2022/6/20
 */
@Getter
@Setter
@ToString
public class PlanScheduler implements Schedulable, Serializable {
    private static final long serialVersionUID = -7080678669095920408L;

    private PlanInfo info;

    /**
     * 最后调度时间
     */
    private Instant lastScheduleAt;

    /**
     * 最后接收反馈时间
     */
    private Instant lastFeedbackAt;

    // --------需注入
    @Getter(AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE, onMethod_ = @Inject)
    @ToString.Exclude
    private transient EventPublisher<Event<?>> eventEventPublisher;

    /**
     * 作业触发计算器
     */
    @ToString.Exclude
    private transient ScheduleCalculator triggerCalculator;

    @Getter(AccessLevel.NONE)
    @Setter(value = AccessLevel.PRIVATE, onMethod_ = @Inject)
    @ToString.Exclude
    private transient ScheduleCalculatorFactory strategyFactory;


    @Override
    public String getId() {
        return info.getPlanId() + ":" + info.getVersion();
    }

    @Override
    public ScheduleOption getScheduleOption() {
        return info.getScheduleOption();
    }

    @Override
    public Instant getLastScheduleAt() {
        return lastScheduleAt;
    }

    @Override
    public Instant getLastFeedbackAt() {
        return lastFeedbackAt;
    }

    @Override
    public void schedule() {
        // 校验能否下发
        List<Job> jobs = info.getDag().getEarliestJobs();
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }

        // 触发调度事件
        // todo 如果 plan 需要持久化，那么持久化一个 PlanRecord 那么如果出现主从切换，从节点会获取到这个数据并执行下发
        //      如果 plan 不需要持久化，那么plan存在内存，如果主节点挂了这次执行可能就会丢失
        eventEventPublisher.publish(new Event<>(this));

        // 此次任务的调度时间
        lastScheduleAt = TimeUtil.nowInstant();
        // 此次任务调度了还没反馈 所以反馈时间为空
        lastFeedbackAt = null;
    }

    /**
     * 计算作业下一次被触发时的时间戳。如果作业不会被触发，返回0或负数；
     * @return 作业下一次被触发时的时间戳，从1970-01-01 00:00:00到触发时刻的毫秒数。
     */
    @Override
    public long nextTriggerAt() {
        return lazyInitTriggerCalculator().apply(this);
    }

    /**
     * 延迟加载作业触发计算器
     */
    protected ScheduleCalculator lazyInitTriggerCalculator() {
        if (triggerCalculator == null) {
            triggerCalculator = strategyFactory.newStrategy(info.getScheduleOption().getScheduleType());
        }

        return triggerCalculator;
    }

}
