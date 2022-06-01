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
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.schedule.Schedulable;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventPublisher;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.JobDAG;
import org.limbo.utils.verifies.Verifies;

import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 计划在具体版本时的数据，至少对应一个{@link Job}
 *
 * @author Brozen
 * @since 2021-10-14
 */
@Getter
@Setter
@ToString
public class PlanInfo implements Schedulable, Serializable {

    private static final long serialVersionUID = -3488415933872953356L;

    /**
     * 作业执行计划ID
     */
    private String planId;

    /**
     * 版本 planId + version 唯一
     */
    private Integer version;

    /**
     * 执行计划描述
     */
    private String description;

    /**
     * 作业计划调度配置参数
     */
    private ScheduleOption scheduleOption;

    /**
     * 作业计划对应的Job，以DAG数据结构组织
     */
    private JobDAG dag;

    @Getter
    private Instant lastScheduleAt;

    @Getter
    private Instant lastFeedbackAt;

    // -------- 需要注入
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    @ToString.Exclude
    @Inject
    private transient ScheduleCalculatorFactory strategyFactory;

    /**
     * 作业触发计算器
     */
    @ToString.Exclude
    private transient ScheduleCalculator triggerCalculator;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @Inject
    private transient EventPublisher<Event<?>> eventEventPublisher;


    public PlanInfo(String planId, Integer version, String description,
                    ScheduleOption scheduleOption, JobDAG dag) {
        this.planId = planId;
        this.version = version;
        this.description = description;
        this.scheduleOption = scheduleOption;
        this.dag = dag;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getId() {
        return planId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void schedule() {
        // 校验能否下发
        List<Job> jobs = getDag().getEarliestJobs();
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
            triggerCalculator = strategyFactory.newStrategy(scheduleOption.getScheduleType());
        }

        return triggerCalculator;
    }


    /**
     * 生成新的计划调度记录
     * @param recordId 调度记录ID
     * @param state 初始化调度记录状态
     * @param manual 是否手动触发调度
     * @return 调度记录状态
     */
    public PlanRecord newRecord(PlanRecord.ID recordId, PlanScheduleStatus state, boolean manual) {
        PlanRecord record = new PlanRecord();
        record.setId(recordId);
        record.setVersion(version);
        record.setRetry(scheduleOption.getRetry());
        record.setDag(dag);
        record.setState(state);
        record.setManual(manual);
        record.setStartAt(TimeUtil.nowInstant());
        return record;
    }


    /**
     * 作业计划数据构造器
     */
    public static class Builder {

        private final ScheduleCalculatorFactory strategyFactory;

        /**
         * 作业计划调度配置参数
         */
        private ScheduleOption scheduleOption;

        /**
         * 设置作业执行计划ID
         */
        private String planId;

        /**
         * 作业计划数据版本号，默认1
         */
        private int version = 1;

        /**
         * 执行计划描述
         */
        private String description;

        /**
         * 作业计划对应的Job列表
         */
        private List<Job> jobs;

        public Builder(ScheduleCalculatorFactory strategyFactory) {
            this.strategyFactory = Verifies.requireNotNull(strategyFactory);
        }


        /**
         * 设置作业执行计划ID，不可为空
         */
        public PlanInfo.Builder planId(@NotBlank String planId) {
            Verifies.notBlank(planId, "planId cannot be blank");
            this.planId = planId;
            return this;
        }


        /**
         * 设置作业计划数据版本号，必须是大于0的正整数
         */
        public PlanInfo.Builder version(@NotNull @Min(1) Integer version) {
            Verifies.verify(version != null && version > 0, "version must be a positive integer");
            this.version = version;
            return this;
        }


        /**
         * 设置执行计划描述
         */
        public PlanInfo.Builder description(String description) {
            this.description = description;
            return this;
        }


        /**
         * 作业计划对应的Job，至少要有一个Job
         */
        public PlanInfo.Builder jobs(@NotNull @Size(min = 1) List<Job> jobs) {
            this.jobs = jobs;
            return this;
        }


        /**
         * 设置作业计划调度配置参数
         */
        public PlanInfo.Builder scheduleOption(ScheduleOption scheduleOption) {
            Verifies.notNull(scheduleOption);
            this.scheduleOption = scheduleOption;
            return this;
        }


        /**
         * 根据配置的参数，构造出作业计划数据
         */
        public PlanInfo build() {
            // 参数校验
            Verifies.requireNotNull(scheduleOption, "scheduleOption is null");

            // 生成
            PlanInfo planInfo = new PlanInfo(
                    planId, version, description,
                    scheduleOption, new JobDAG(jobs)
            );
            planInfo.setStrategyFactory(strategyFactory);

            return planInfo;
        }
    }

}
