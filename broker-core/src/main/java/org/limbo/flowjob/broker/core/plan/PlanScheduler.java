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
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.broker.WorkerManager;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.exceptions.JobExecuteException;
import org.limbo.flowjob.broker.core.plan.job.Job;
import org.limbo.flowjob.broker.core.plan.job.context.JobInstance;
import org.limbo.flowjob.broker.core.plan.job.context.Task;
import org.limbo.flowjob.broker.core.plan.job.context.TaskCreateStrategyFactory;
import org.limbo.flowjob.broker.core.repositories.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.repositories.TaskRepository;
import org.limbo.flowjob.broker.core.schedule.Schedulable;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.broker.core.utils.TimeUtil;
import org.limbo.flowjob.broker.core.worker.Worker;

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
    /**
     * 作业触发计算器
     */
    @ToString.Exclude
    private transient ScheduleCalculator triggerCalculator;

    @Getter(AccessLevel.NONE)
    @Setter(value = AccessLevel.PUBLIC, onMethod_ = @Inject)
    @ToString.Exclude
    private transient ScheduleCalculatorFactory strategyFactory;

    @ToString.Exclude
    @Setter(value = AccessLevel.PUBLIC, onMethod_ = @Inject)
    private transient PlanInstanceRepository planInstanceRepo;

    @ToString.Exclude
    @Setter(value = AccessLevel.PUBLIC, onMethod_ = @Inject)
    private transient JobInstanceRepository jobInstanceRepo;

    @ToString.Exclude
    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepo;

    @ToString.Exclude
    @Setter(onMethod_ = @Inject)
    private TaskCreateStrategyFactory taskCreateStrategyFactory;

    @ToString.Exclude
    @Setter(onMethod_ = @Inject)
    private WorkerManager workerManager;

    @ToString.Exclude
    @Setter(onMethod_ = @Inject)
    private WorkerSelectorFactory workerSelectorFactory;


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String getId() {
        return info.getPlanId() + ":" + info.getVersion();
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public ScheduleOption getScheduleOption() {
        return info.getScheduleOption();
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Instant getLastScheduleAt() {
        return lastScheduleAt;
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Instant getLastFeedbackAt() {
        return lastFeedbackAt;
    }


    /**
     * 执行 Plan 下发：
     * 1. 校验 DAG 作业图
     */
    @Override
    public void schedule() {
        // 获取 DAG 中最执行的作业，如不存在说明 Plan 无需下发
        List<Job> jobs = info.getDag().getEarliestJobs();
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }

        // todo 如果 plan 需要持久化，那么持久化一个 PlanRecord 那么如果出现主从切换，从节点会获取到这个数据并执行下发
        //      如果 plan 不需要持久化，那么plan存在内存，如果主节点挂了这次执行可能就会丢失

        // 先生成执行计划实例
        PlanInstance planInstance = this.info.newInstance(PlanScheduleStatus.SCHEDULING, false);
        String planInstanceId = planInstanceRepo.add(planInstance);
        planInstance.setPlanInstanceId(planInstanceId);

        // 生成作业实例
        for (Job job : jobs) {
            JobInstance jobInstance = job.newInstance(this.info.getPlanId(), planInstanceId, JobScheduleStatus.SCHEDULING);
            String jobInstanceId = jobInstanceRepo.add(jobInstance);
            jobInstance.setJobInstanceId(jobInstanceId);

            // 将作业对应的任务信息下发给worker
            dispatchTask(job, jobInstance);
        }

        // 更新此次调度时间
        lastScheduleAt = TimeUtil.nowInstant();
        // 此次任务调度了还没反馈 所以反馈时间为空
        lastFeedbackAt = null;
    }


    /**
     * 作业下发，会先持久化任务，然后执行下发
     * @param job 作业
     * @param instance 待下发的作业实例
     */
    private void dispatchTask(Job job, JobInstance instance) {
        // todo 下发前确认下对应的jobInstance是否已经关闭

        // 生成并持久化Task
        TaskCreateStrategyFactory.TaskCreateStrategy taskCreateStrategy
                = taskCreateStrategyFactory.newStrategy(instance.getTaskType());
        Task task = taskCreateStrategy.apply(instance);
        taskRepo.add(task);

        // 选择worker
        WorkerSelector workerSelector = workerSelectorFactory.newSelector(job.getDispatchOption().getLoadBalanceType());
        if (workerSelector == null) {
            throw new JobExecuteException(job.getJobId(),
                    "Cannot create JobDispatcher for dispatch type: " + job.getDispatchOption().getLoadBalanceType());
        }
        Worker worker = workerSelector.select(task, workerManager.availableWorkers());

        // 执行下发
        boolean dispatched = task.dispatch(worker);

        // 根据下发结果，更新作业实例状态
        if (dispatched) {
            instance.dispatched();
        } else {
            // warning 注意，task下发失败不在这里处理，封装一个 RetryableTask（装饰or继承Task），在内部实现重试
            //         所以这里只需要记录状态就好了
            instance.dispatchFailed();
        }
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
