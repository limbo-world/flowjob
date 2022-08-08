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
import org.limbo.flowjob.broker.api.constants.enums.PlanStatus;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventCenter;
import org.limbo.flowjob.broker.core.plan.job.JobInfo;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;
import org.limbo.flowjob.broker.core.plan.job.dag.DAG;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.schedule.Schedulable;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.common.utils.TimeUtil;

import javax.inject.Inject;
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
    private PlanStatus status;

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
    private TriggerType triggerType;

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
    private DAG<JobInfo> dag;

    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private transient List<JobInstance> jobInstances;

    @Setter(onMethod_ = @Inject)
    private transient JobInstanceRepository jobInstanceRepo;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private transient ScheduleCalculator triggerCalculator;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private transient ScheduleCalculatorFactory strategyFactory;


    /**
     * 检测 Plan 实例是否已经执行完成
     */
    public boolean isFinished() {
        return checkFinished(dag.nodes());
    }


    /**
     * todo 判断作业是否可以被触发。检测作业在 DAG 中的前置作业节点是否均可触发子节点。
     */
    public boolean isJobTriggered(JobInfo jobInfo) {
        return checkFinished(dag.preNodes(jobInfo.getJobId()));
    }


    /**
     * 判断某一计划实例中，一批作业是否全部可以触发下一步
     */
    private boolean checkFinished(List<JobInfo> jobInfos) {
        // 有实例还未创建直接返回
        if (jobInfos.size() > jobInstances.size()) {
            return false;
        }

        // 判断是否所有实例都可以触发下个任务
        for (JobInstance jobInstance : jobInstances) {
            if (!jobInstance.getStatus().isCompleted()) {
                return false;
            }
        }

        return true;
    }


    /**
     * 计划执行成功
     */
    public void executeSucceed() {
        if (PlanStatus.EXECUTING != status) {
            return;
        }

        setStatus(PlanStatus.SUCCEED);

        EventCenter.publish(new Event(this, ScheduleEventTopic.PLAN_SUCCESS));
    }

    /**
     * 计划执行成功
     */
    public void executeFailed() {
        if (PlanStatus.EXECUTING != status) {
            return;
        }

        setStatus(PlanStatus.FAILED);

        EventCenter.publish(new Event(this, ScheduleEventTopic.PLAN_FAIL));
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
        if (PlanStatus.SCHEDULING != getStatus()) {
            return;
        }
        setStatus(PlanStatus.EXECUTING);
        EventCenter.publish(new Event(this, ScheduleEventTopic.PLAN_EXECUTING));
        try {
            for (JobInstance jobInstance : scheduleJobInstances()) {
                // todo 多线程 -- 需要考虑并发下状态变更问题
                jobInstance.dispatch();
            }
        } catch (Exception e) {
            log.error("[PlanInstance] schedule fail planInstance:{}", this, e);
        }
    }

    // todo 移除？？？
    public List<JobInstance> scheduleJobInstances() {
        if (jobInstances == null) {
            jobInstances = new ArrayList<>();
        }
        // todo 获取
        if (CollectionUtils.isNotEmpty(jobInstances)) {
            return null;
        }
        // 获取 DAG 中最执行的作业，如不存在说明 Plan 无需下发
        List<JobInfo> jobInfos = dag.roots();
        if (CollectionUtils.isEmpty(jobInfos)) {
            return jobInstances;
        }
        for (JobInfo jobInfo : jobInfos) {
            // 下发task
            JobInstance jobInstance = jobInfo.newInstance(planId, planInstanceId);
            if (TriggerType.SCHEDULE != jobInfo.getTriggerType()) {
                continue;
            }
            jobInstances.add(jobInstance);
        }
        return jobInstances;
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
        Long nextTriggerAt = lazyInitTriggerCalculator().calculate(this);
        return TimeUtil.toLocalDateTime(nextTriggerAt);
    }

    /**
     * 延迟加载作业触发计算器
     */
    protected ScheduleCalculator lazyInitTriggerCalculator() {
        if (triggerCalculator == null) {
            triggerCalculator = strategyFactory.apply(scheduleOption.getScheduleType());
        }

        return triggerCalculator;
    }

    /**
     * 下发后续任务
     */
    public void dispatchNext(String jobId) {
        List<JobInfo> subJobInfos = dag.subNodes(jobId);

        if (CollectionUtils.isEmpty(subJobInfos)) {

            // 后续作业不存在，需检测是否 Plan 执行完成
            if (isFinished()) {
                executeSucceed();
            }

        } else {

            // todo 发送事件，保存数据

            // 后续作业存在，则检测是否可触发，并继续下发作业
            for (JobInfo subJobInfo : subJobInfos) {
                if (isJobTriggered(subJobInfo)) {
                    JobInstance subJobInstance = subJobInfo.newInstance(planId, planInstanceId);
                    subJobInstance.dispatch();
                }
            }

        }
    }

    /**
     * 重试job
     */
    public boolean needRetryJob(JobInstance jobInstance) {
        // todo
        return false;
    }

}
