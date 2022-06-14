/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.limbo.flowjob.broker.core.repositories.PlanInfoRepository;
import org.limbo.flowjob.broker.core.repositories.PlanRepository;
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
 * 执行计划。一个计划{@link Plan}对应至少一个作业{@link Job}
 *
 * @author Brozen
 * @since 2021-07-12
 */
@Getter
@Setter
@ToString
public class Plan implements Schedulable, Serializable {

    private static final long serialVersionUID = 5657376836197403211L;

    /**
     * 执行计划ID
     */
    private Long planId;

    /**
     * 当前版本
     */
    private String currentVersion;

    /**
     * 最新版本
     */
    private String recentlyVersion;

    /**
     * 当前版本的具体信息
     */
    private PlanInfo info;

    /**
     * 最后调度时间
     */
    @Getter
    private Instant lastScheduleAt;

    /**
     * 最后接收反馈时间
     */
    @Getter
    private Instant lastFeedbackAt;

    /**
     * 是否已启用
     */
    private boolean isEnabled;

    // --------需注入
    @ToString.Exclude
    @Inject
    private PlanInfoRepository planInfoRepository;

    @ToString.Exclude
    @Inject
    private PlanRepository planRepository;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    @ToString.Exclude
    @Inject
    private transient ScheduleCalculatorFactory strategyFactory;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @Inject
    private transient EventPublisher<Event<?>> eventEventPublisher;

    /**
     * 作业触发计算器
     */
    @ToString.Exclude
    private transient ScheduleCalculator triggerCalculator;


    /**
     * 启用当前计划
     */
    public boolean enable() {
        boolean succeed = planRepository.enablePlan(this) == 1;
        if (succeed) {
            this.isEnabled = true;
        }
        return succeed;
    }


    /**
     * 停用当前计划
     */
    public boolean disable() {
        boolean succeed = planRepository.disablePlan(this) == 1;
        if (succeed) {
            this.isEnabled = false;
        }
        return succeed;
    }

    /**
     * 更新执行计划信息，版本号递增
     * @param planInfo 执行计划信息
     */
    public void addNewVersion(PlanInfo planInfo) {
        // 为 PlanInfo 设置版本号
        int newVersion = getRecentlyVersion() + 1;
        planInfo.setVersion(newVersion);
        info = planInfo;

        // 更新当前使用版本信息
        newVersion = planRepository.updateVersion(this, newVersion);

        // 更新领域对象中的版本号
        currentVersion = newVersion;
        recentlyVersion = newVersion;
    }

    @Override
    public String getId() {
        return planId;
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
