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
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.JobInstanceFactory;
import org.limbo.flowjob.broker.core.schedule.Calculated;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.Scheduled;
import org.limbo.flowjob.broker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.common.constants.PlanStatus;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.time.TimeUtils;

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
     * 期望的触发时间
     */
    private LocalDateTime expectTriggerAt;

    /**
     * 触发类型 todo 这个有啥用？ 和 job 上的触发类型一样的？
     */
    private TriggerType triggerType;

    /**
     * 真正的触发时间 -- 开始时间
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

    /**
     * 全局上下文 配置 --- 或者job执行中变更 整个plan生命周期内传递
     */
    private Attributes context;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private transient ScheduleCalculator scheduleCalculator;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private List<JobInstance> rootJobs;

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
        return TimeUtils.toLocalDateTime(nextTriggerAt);
    }

    /**
     * 延迟加载作业触发计算器
     */
    protected ScheduleCalculator lazyInitTriggerCalculator() {
        if (scheduleCalculator == null) {
            scheduleCalculator = ScheduleCalculatorFactory.create(scheduleOption.getScheduleType());
        }

        return scheduleCalculator;
    }

    public List<JobInstance> rootJobs() {
        if (rootJobs != null) {
            return rootJobs;
        }
        rootJobs = new ArrayList<>();
        for (JobInfo jobInfo : dag.origins()) {
            if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                rootJobs.add(JobInstanceFactory.create(this, jobInfo, TimeUtils.currentLocalDateTime()));
            }
        }
        return rootJobs;
    }

}
