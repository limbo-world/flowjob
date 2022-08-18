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

package org.limbo.flowjob.broker.core.domain.plan;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.constants.enums.PlanStatus;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.core.domain.factory.JobInstanceFactory;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.schedule.Calculated;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.Scheduled;
import org.limbo.flowjob.broker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.common.utils.TimeUtil;
import org.limbo.flowjob.common.utils.dag.DAG;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个调度的plan实例
 *
 * @author Devil
 * @since 2021/9/1
 */
@Slf4j
@Data
public class PlanInstance implements Scheduled, Calculated, Serializable {

    private static final long serialVersionUID = 1837382860200548371L;

    private String planInstanceId;

    private String planId;

    /**
     * 计划的版本
     */
    private String version;

    /**
     * 计划调度状态
     */
    private PlanStatus status;

    /**
     * 作业计划调度配置参数
     */
    private ScheduleOption scheduleOption;

    /**
     * 触发时间
     */
    private LocalDateTime expectTriggerAt;

    /**
     * 触发类型
     */
    private TriggerType triggerType;

    /**
     * 开始时间
     */
    private LocalDateTime triggerAt;

    /**
     * 结束时间
     */
    private LocalDateTime feedbackAt;

    /**
     * 执行图
     */
    private DAG<JobInfo> dag;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private transient ScheduleCalculator scheduleCalculator;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private transient ScheduleCalculatorFactory strategyFactory;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private transient JobInstanceFactory jobInstanceFactory;

    @Override
    public String scheduleId() {
        return planId + ":" + expectTriggerAt;
    }

    @Override
    public ScheduleOption scheduleOption() {
        return scheduleOption;
    }

    @Override
    public LocalDateTime lastScheduleAt() {
        return triggerAt;
    }

    @Override
    public LocalDateTime lastFeedbackAt() {
        return feedbackAt;
    }

    public List<JobInstance> getRootJobs() {
        List<JobInstance> jobInstances = new ArrayList<>();
        for (JobInfo jobInfo : dag.roots()) {
            if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                jobInstances.add(jobInstanceFactory.create(planInstanceId, jobInfo, TimeUtil.currentLocalDateTime()));
            }
        }
        return jobInstances;
    }

    @Override
    public LocalDateTime triggerAt() {
        return expectTriggerAt;
    }

    /**
     * 计算下次触发时间
     */
    @Override
    public LocalDateTime nextTriggerAt() {
        Long nextTriggerAt = lazyInitTriggerCalculator().calculate(this);
        return TimeUtil.toLocalDateTime(nextTriggerAt);
    }

    /**
     * 延迟加载作业触发计算器
     */
    protected ScheduleCalculator lazyInitTriggerCalculator() {
        if (scheduleCalculator == null) {
            scheduleCalculator = strategyFactory.apply(scheduleOption.getScheduleType());
        }

        return scheduleCalculator;
    }

    @Getter
    @AllArgsConstructor
    public static class JobInstances implements Serializable {
        private static final long serialVersionUID = 2475726765959050169L;

        private String planInstanceId;

        private List<JobInstance> instances;

    }

}
