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
import org.limbo.flowjob.broker.core.plan.job.context.TaskCreatorFactory;
import org.limbo.flowjob.broker.core.plan.job.dag.DAG;
import org.limbo.flowjob.broker.core.schedule.Calculated;
import org.limbo.flowjob.broker.core.schedule.ScheduleCalculator;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.Scheduled;
import org.limbo.flowjob.broker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.broker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.common.utils.TimeUtil;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 数据映射
     */
    @Setter(AccessLevel.NONE)
    private Map<String, List<JobInstance>> jobInstanceMap = new HashMap<>();

    /**
     * 未下发的job
     */
    @Setter(AccessLevel.NONE)
    private List<JobInstance> unScheduledJobInstances = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private transient ScheduleCalculator scheduleCalculator;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private transient ScheduleCalculatorFactory strategyFactory;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private transient Scheduler scheduler;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private transient TaskCreatorFactory taskCreatorFactory;

    /**
     * 判断某一计划实例中，一批作业是否全部可以触发下一步
     */
    private boolean checkFinished(List<JobInfo> jobInfos) {
        if (CollectionUtils.isEmpty(jobInfos)) {
            return true;
        }

        for (JobInfo jobInfo : jobInfos) {
            List<JobInstance> jobInstanceList = jobInstanceMap.get(jobInfo.getId());
            if (CollectionUtils.isEmpty(jobInstanceList)) {
                return false;
            }
            if (!jobInstanceList.get(0).isCompleted()) {
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
    public void schedule() {
        if (PlanStatus.SCHEDULING != getStatus()) {
            return;
        }
        setStatus(PlanStatus.EXECUTING);
        setTriggerAt(TimeUtil.currentLocalDateTime());

        // 获取 DAG 中最执行的作业，如不存在说明 Plan 无需下发
        List<JobInfo> jobInfos = dag.roots();
        for (JobInfo jobInfo : jobInfos) {
            createScheduleJob(jobInfo);
        }

        EventCenter.publish(new Event(this, ScheduleEventTopic.PLAN_EXECUTING));

        for (JobInstance jobInstance : unScheduledJobInstances) {
            scheduler.schedule(jobInstance);
        }
        unScheduledJobInstances.clear();
    }

    private void createScheduleJob(JobInfo jobInfo) {
        if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
            JobInstance jobInstance = jobInfo.newInstance(planInstanceId, taskCreatorFactory, TimeUtil.currentLocalDateTime());
            putJobInstance(jobInstance);
            unScheduledJobInstances.add(jobInstance);
        }
    }

    public void putJobInstance(JobInstance jobInstance) {
        String jobId = jobInstance.getJobId();
        jobInstanceMap.putIfAbsent(jobId, new ArrayList<>());
        jobInstanceMap.get(jobId).add(jobInstance);

        // todo 根据创建时间排序
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

    /**
     * 下发后续任务
     */
    public void dispatchNext(String jobId) {
        List<JobInfo> subJobInfos = dag.subNodes(jobId);

        if (CollectionUtils.isEmpty(subJobInfos)) {
            // 检测 Plan 实例是否已经执行完成
            if (checkFinished(dag.getLeafNodes())) {
                executeSucceed();
            }
        } else {

            // 后续作业存在，则检测是否可触发，并继续下发作业
            for (JobInfo subJobInfo : subJobInfos) {
                if (checkFinished(dag.preNodes(subJobInfo.getId()))) {
                    createScheduleJob(subJobInfo);
                }
            }

            EventCenter.publish(new Event(this, ScheduleEventTopic.PLAN_DISPATCH_NEXT));

            for (JobInstance subJobInstance : unScheduledJobInstances) {
                scheduler.schedule(subJobInstance);
            }
            unScheduledJobInstances.clear();

        }
    }

    /**
     * 是否需要重试job
     */
    public boolean needRetryJob(String jobId) {
        JobInfo jobInfo = dag.getNode(jobId);
        List<JobInstance> jobInstances = jobInstanceMap.get(jobId);
        if (CollectionUtils.isEmpty(jobInstances)) {
            return true;
        }
        return jobInfo.getDispatchOption().getRetry() > jobInstances.size();
    }

    public JobInfo getJobInfo(String jobId) {
        return dag.getNode(jobId);
    }

}
