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
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.JobTriggerType;
import org.limbo.flowjob.broker.api.constants.enums.PlanTriggerType;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.exceptions.JobExecuteException;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.JobDAG;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstance;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.plan.job.context.TaskCreateStrategyFactory;
import org.limbo.flowjob.broker.core.repositories.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanRepository;
import org.limbo.flowjob.broker.core.repositories.TaskRepository;
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
    private long triggerAt;

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
    private transient TaskRepository taskRepo;

    @ToString.Exclude
    @Setter(onMethod_ = @Inject)
    private transient TaskCreateStrategyFactory taskCreateStrategyFactory;

    @ToString.Exclude
    @Setter(onMethod_ = @Inject)
    private transient WorkerSelectorFactory workerSelectorFactory;

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
        // 更新状态
        setStatus(PlanScheduleStatus.SUCCEED);
        planInstanceRepo.executeSucceed(this);

        // todo 检测 Plan 是否需要重新调度，只有 FIXED_INTERVAL 类型的计划，需要在完成时扔到时间轮里重新调度，手动的和其他的都不需要
//        PlanScheduler planScheduler = planSchedulerRepo.get(this.version);
//        ScheduleType scheduleType = scheduleOption.getScheduleType();
//        if (ScheduleType.FIXED_INTERVAL == scheduleType && isManual()) {
//            planScheduler.setLastScheduleAt(this.startAt);
//            planScheduler.setLastFeedbackAt(TimeUtil.nowInstant());
////            trackerNode.jobTracker().schedule(planScheduler);
//        }
    }


    /**
     * 计划执行成功
     */
    public void executeFailed() {
        // 更新状态
        setStatus(PlanScheduleStatus.FAILED);
        planInstanceRepo.executeFailed(this);
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
    public LocalDateTime scheduleAt() {
        return scheduleAt;
    }

    @Override
    public LocalDateTime feedbackAt() {
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
                JobInstance jobInstance = job.newInstance(this);
                // 下发task
                ScheduleThreadPool.TASK_DISPATCH_POOL.submit(() -> {
                    try {
                        if (JobTriggerType.SCHEDULE != job.getTriggerType()) {
                            return;
                        }
                        jobInstanceRepo.add(jobInstance);

                        // 将作业对应的任务信息下发给worker
                        dispatchTask(job, jobInstance);
                    } catch (Exception e) {
                        log.error("[PlanInstance] dispatchJob fail job:{}", job, e);
                    }
                });
            }

            // 更新plan的下次触发时间 todo 如果上面执行完到这步失败--可能导致重复下发 所以需要对job和task的数据进行检测
            Long nextTriggerAt = lazyInitTriggerCalculator().apply(this);
            planRepository.nextTriggerAt(planId, TimeUtil.toLocalDateTime(nextTriggerAt));
        } catch (Exception e) {
            log.error("[PlanInstance] schedule fail planInstance:{}", this, e);
        }
    }

    /**
     * 作业下发，会先持久化任务，然后执行下发
     *
     * @param job      作业
     * @param instance 待下发的作业实例
     */
    public void dispatchTask(Job job, JobInstance instance) {
        // todo 下发前确认下对应的jobInstance是否已经关闭--可能重复下发？

        // 生成并持久化Task
        TaskCreateStrategyFactory.TaskCreateStrategy taskCreateStrategy = taskCreateStrategyFactory.newStrategy(instance.getTaskType());
        Task task = taskCreateStrategy.apply(instance);
        taskRepo.add(task);

        // 选择worker
        WorkerSelector workerSelector = workerSelectorFactory.newSelector(job.getDispatchOption().getLoadBalanceType());
        if (workerSelector == null) {
            throw new JobExecuteException(job.getJobId(),
                    "Cannot create JobDispatcher for dispatch type: " + job.getDispatchOption().getLoadBalanceType());
        }

        // 执行下发
        boolean dispatched = task.dispatch(workerSelector);

        // 根据下发结果，更新作业实例状态
        if (dispatched) {
            instance.dispatched();
        } else {
            // warning 注意，task下发失败不在这里处理，封装一个 RetryableTask（装饰or继承Task），在内部实现重试
            //         所以这里只需要记录状态就好了
            // todo 下发失败判断是否有重试 根据重试机制处理
            instance.dispatchFailed();
        }
    }

    @Override
    public long triggerAt() {
        return triggerAt;
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
