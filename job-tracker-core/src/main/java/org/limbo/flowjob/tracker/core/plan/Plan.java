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

package org.limbo.flowjob.tracker.core.plan;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.utils.strategies.StrategyFactory;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.tracker.core.schedule.executor.Executor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 计划的抽象。一个计划{@link Plan}对应至少一个作业{@link Job}
 *
 * @author Brozen
 * @since 2021-07-12
 */
@Getter
@Setter
@ToString
public class Plan implements Schedulable {

    /**
     * 作业计划ID
     */
    private String planId;

    /**
     * 当前版本
     */
    private Integer version;

    /**
     * 计划描述
     */
    private String planDesc;

    /**
     * 作业计划调度配置参数
     */
    private ScheduleOption scheduleOption;

    /**
     * 此执行计划对应的所有作业
     */
    private List<Job> jobs;

    // -------- 需要注入
    /**
     * 作业触发计算器
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private ScheduleCalculator triggerCalculator;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private Executor<Plan> executor;

    public Plan(ScheduleCalculator triggerCalculator, Executor<Plan> executor) {
        this.triggerCalculator = triggerCalculator;
        this.executor = executor;
    }

    /**
     * 计算作业下一次被触发时的时间戳。如果作业不会被触发，返回0或负数；
     * @return 作业下一次被触发时的时间戳，从1970-01-01 00:00:00到触发时刻的毫秒数。
     */
    public long nextTriggerAt() {
        return triggerCalculator.apply(this);
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
     * TODO
     */
    @Override
    public Instant getLastScheduleAt() {
        return null;
    }


    /**
     * TODO
     * @return
     */
    @Override
    public Instant getLastFeedbackAt() {
        return null;
    }

    @Override
    public void schedule() {
        executor.execute(this);
    }

    /**
     * 生成新的计划实例
     * @return 实例
     */
    public PlanInstance newInstance(Long planInstanceId, PlanScheduleStatus state) {
        PlanInstance instance = new PlanInstance();
        instance.setPlanId(planId);
        instance.setVersion(version);
        instance.setPlanInstanceId(planInstanceId);
        instance.setState(state);
        instance.setJobs(jobs);
        return instance;
    }


    public List<Job> getJobs() {
        return jobs == null ? new ArrayList<>() : jobs;
    }

    /**
     * 查询此plan下的job
     * @param jobId 作业ID
     * @return 作业领域
     */
    public Job getJob(String jobId) {
        if (CollectionUtils.isEmpty(jobs)) {
            return null;
        }
        return null;
//        return jobRepository.getJob(jobId);
    }

    public static class Builder {

        private StrategyFactory<ScheduleType, ScheduleCalculator, Schedulable, Long> strategyFactory;

        private ScheduleCalculator triggerCalculator;

        private Executor<Plan> executor;

        private ScheduleOption scheduleOption;

        private String planId;

        private Integer version;

        private String planDesc;

        private List<Job> jobs;

        public Builder(StrategyFactory<ScheduleType, ScheduleCalculator, Schedulable, Long> strategyFactory, Executor<Plan> executor) {
            this.strategyFactory = strategyFactory;
            this.executor = executor;
        }

        public Builder planId(String planId) {
            this.planId = planId;
            return this;
        }

        public Builder version(Integer version) {
            this.version = version;
            return this;
        }

        public Builder planDesc(String planDesc) {
            this.planDesc = planDesc;
            return this;
        }

        public Builder jobs(List<Job> jobs) {
            this.jobs = jobs;
            return this;
        }

        public Builder scheduleOption(ScheduleOption scheduleOption) {
            ScheduleType scheduleType = scheduleOption.getScheduleType();

            this.triggerCalculator = strategyFactory.newStrategy(scheduleType);
            this.scheduleOption = scheduleOption;
            return this;
        }

        public Plan build() {
            Plan plan = new Plan(triggerCalculator, executor);
            plan.setPlanId(planId);
            plan.setVersion(version);
            plan.setPlanDesc(planDesc);
            plan.setScheduleOption(scheduleOption);
            plan.setJobs(jobs);
            return plan;
        }

    }

}
