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
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.JobNodeType;
import org.limbo.flowjob.tracker.commons.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.tracker.commons.constants.enums.ScheduleType;
import org.limbo.flowjob.tracker.commons.utils.strategies.StrategyFactory;
import org.limbo.flowjob.tracker.core.job.Job;
import org.limbo.flowjob.tracker.core.schedule.Schedulable;
import org.limbo.flowjob.tracker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.tracker.core.schedule.executor.Executor;
import org.limbo.utils.UUIDUtils;
import org.limbo.utils.verifies.Verifies;

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
     * todo jobs为空的情况
     */
    private List<Job> jobs;

    private Job startJob;

    private Instant lastScheduleAt;

    private Instant lastFeedBackAt;

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

    @Override
    public Instant getLastScheduleAt() {
        return lastScheduleAt;
    }

    @Override
    public Instant getLastFeedbackAt() {
        return lastFeedBackAt;
    }

    @Override
    public void schedule() {
        // 执行调度
        executor.execute(this);
        // 此次任务的调度时间
        lastScheduleAt = Instant.now();
        // 此次任务调度了还没反馈 所以反馈时间为空
        lastFeedBackAt = null;
    }

    /**
     * 生成新的计划实例
     * @return 实例
     */
    public PlanInstance newInstance(Long planInstanceId, PlanScheduleStatus state, boolean reschedule) {
        PlanInstance instance = new PlanInstance();
        instance.setPlanId(planId);
        instance.setVersion(version);
        instance.setPlanInstanceId(planInstanceId);
        instance.setState(state);
        instance.setReschedule(reschedule);
        instance.setJobs(jobs);
        instance.setStartAt(Instant.now());
        return instance;
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
        for (Job job : jobs) {
            if (job.getJobId().equals(jobId)) {
                return job;
            }
        }
        return null;
    }

    public Job getStartJob() {
        if (startJob != null) {
            return startJob;
        }
        for (Job job : jobs) {
            if (JobNodeType.START == job.getNodeType()) {
                startJob = job;
                break;
            }
        }
        return startJob;
    }

    /**
     * 获取最先需要执行的job 因为 DAG 可能会有多个一起执行
     * @return 最先执行的 job
     */
    public List<Job> getEarliestJobs() {
        return getStartJob().getChildren();
    }

    /**
     * 获取job后续的作业
     */
    public List<Job> getSubJobs(String jobId) {
        return getJob(jobId).getChildren();
    }

    /**
     * 获取job前置的作业
     */
    public List<Job> getPreJobs(String jobId) {
        return getJob(jobId).getParents();
    }


    /**
     * Plan领域对象的Builder模式封装
     */
    public static class Builder {

        private final StrategyFactory<ScheduleType, ScheduleCalculator, Schedulable, Long> strategyFactory;

        private ScheduleCalculator triggerCalculator;

        private final Executor<Plan> executor;

        private ScheduleOption scheduleOption;

        private String planId;

        private Integer version;

        private String planDesc;

        private List<Job> jobs;

        public Builder(StrategyFactory<ScheduleType, ScheduleCalculator, Schedulable, Long> strategyFactory, Executor<Plan> executor) {
            this.strategyFactory = Verifies.requireNotNull(strategyFactory);
            this.executor = Verifies.requireNotNull(executor);
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
            Verifies.requireNotNull(triggerCalculator, "triggerCalculator is null");
            Verifies.requireNotNull(executor, "executor is null");
            Verifies.requireNotNull(scheduleOption, "scheduleOption is null");

            Plan plan = new Plan(triggerCalculator, executor);
            plan.setPlanId(StringUtils.isNotBlank(planId) ? planId : UUIDUtils.randomID());
            plan.setVersion(version == null ? 1 : version);
            plan.setPlanDesc(planDesc);
            plan.setScheduleOption(scheduleOption);
            plan.setJobs(jobs == null ? new ArrayList<>() : jobs);
            return plan;
        }

    }

}
