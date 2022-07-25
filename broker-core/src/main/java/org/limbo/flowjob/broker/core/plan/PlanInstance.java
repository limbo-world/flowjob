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
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.constants.enums.JobTriggerType;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.PlanTriggerType;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.JobDAG;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstance;
import org.limbo.flowjob.broker.core.repositories.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanRepository;
import org.limbo.flowjob.broker.core.schedule.Schedulable;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.broker.core.utils.TimeUtil;

import javax.inject.Inject;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 一个调度的plan实例
 *
 * @author Devil
 * @since 2021/9/1
 */
@Slf4j
@Data
public class PlanInstance implements Schedulable, Serializable {

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
    private PlanScheduleStatus status;

    /**
     * 作业计划调度配置参数
     */
    private ScheduleOption scheduleOption;

    /**
     * 触发时间
     */
    private LocalDateTime triggerAt;

    /**
     * 触发类型
     */
    private PlanTriggerType triggerType;

    /**
     * 开始时间
     */
    private LocalDateTime scheduleAt;

    /**
     * 结束时间
     */
    private LocalDateTime feedbackAt;

    /**
     * 执行图
     */
    private JobDAG dag;

    // ---------------- 需注入
    @Setter(onMethod_ = @Inject)
    private transient PlanInstanceRepository planInstanceRepo;

    @Setter(onMethod_ = @Inject)
    private transient JobInstanceRepository jobInstanceRepo;

    @ToString.Exclude
    private transient ScheduleCalculator triggerCalculator;

    @Getter(AccessLevel.NONE)
    @Setter(value = AccessLevel.PUBLIC, onMethod_ = @Inject)
    @ToString.Exclude
    private transient ScheduleCalculatorFactory strategyFactory;

    @ToString.Exclude
    @Setter(onMethod_ = @Inject)
    private transient PlanRepository planRepository;

    /**
     * 检测 Plan 实例是否已经执行完成
     */
    public boolean isAllJobFinished() {
        return checkJobsFinished(dag.jobs());
    }


    /**
     * todo 判断作业是否可以被触发。检测作业在 DAG 中的前置作业节点是否均可触发子节点。
     */
    public boolean isJobTriggerable(Job job) {
        return checkJobsFinished(dag.getPreJobs(job.getJobId()));
    }


    /**
     * 判断某一计划实例中，一批作业是否全部可以触发下一步
     */
    private boolean checkJobsFinished(List<Job> jobs) {
        // 获取db中 job实例
        Set<String> jobIds = jobs.stream()
                .map(Job::getJobId)
                .collect(Collectors.toSet());
        List<JobInstance> jobInstances = jobInstanceRepo.listInstances(planInstanceId, jobIds);

        // 有实例还未创建直接返回
        if (jobs.size() > jobInstances.size()) {
            return false;
        }

        // 判断是否所有实例都可以触发下个任务
        for (JobInstance jobInstance : jobInstances) {
            if (!jobInstance.canTriggerNext()) {
                return false;
            }
        }

        return true;
    }


    /**
     * 计划执行成功
     */
    public void executeSucceed() {
        setStatus(PlanScheduleStatus.SUCCEED);
        planInstanceRepo.executeSucceed(this);

        planRepository.nextTriggerAt(planId, nextTriggerAt());
    }

    /**
     * 计划执行成功
     */
    public void executeFailed() {
        setStatus(PlanScheduleStatus.FAILED);
        planInstanceRepo.executeSucceed(this);

        planRepository.nextTriggerAt(planId, nextTriggerAt());
    }

    @Override
    public String scheduleId() {
        return planId + ":" + triggerAt;
    }

    @Override
    public ScheduleOption scheduleOption() {
        return scheduleOption;
    }

    @Override
    public LocalDateTime lastScheduleAt() {
        return scheduleAt;
    }

    @Override
    public LocalDateTime lastFeedbackAt() {
        return feedbackAt;
    }

    @Override
    public void schedule() {
        try {
            // 获取 DAG 中最执行的作业，如不存在说明 Plan 无需下发
            List<Job> jobs = dag.getEarliestJobs();
            if (CollectionUtils.isEmpty(jobs)) {
                return;
            }

            // 生成作业实例
            for (Job job : jobs) {
                // 下发task
                JobInstance jobInstance = job.newInstance(this);
                if (JobTriggerType.SCHEDULE != job.getTriggerType()) {
                    return;
                }
                jobInstance.dispatch();
            }

            // 更新plan的下次触发时间 todo 如果上面执行完到这步失败--可能导致重复下发 所以需要对job和task的数据进行检测
            planRepository.nextTriggerAt(planId, nextTriggerAt());
        } catch (Exception e) {
            log.error("[PlanInstance] schedule fail planInstance:{}", this, e);
        }
    }

    @Override
    public LocalDateTime triggerAt() {
        return triggerAt;
    }

    /**
     * 计算下次触发时间
     */
    @Override
    public LocalDateTime nextTriggerAt() {
        Long nextTriggerAt = lazyInitTriggerCalculator().apply(this);
        return TimeUtil.toLocalDateTime(nextTriggerAt);
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

}
