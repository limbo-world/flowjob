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

package org.limbo.flowjob.broker.dao.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.SinglePlan;
import org.limbo.flowjob.broker.core.domain.plan.WorkflowPlan;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.PlanScheduleTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.TaskScheduleTask;
import org.limbo.flowjob.broker.core.schedule.strategy.IPlanScheduleStrategy;
import org.limbo.flowjob.broker.core.schedule.strategy.ITaskScheduleStrategy;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.constants.TaskType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.exception.VerifyException;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 基础信息转换 静态方法
 *
 * @author Devil
 * @since 2022/8/11
 */
@Slf4j
@Component
public class DomainConverter {

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private IPlanScheduleStrategy planScheduleStrategy;

    @Setter(onMethod_ = @Inject)
    private ITaskScheduleStrategy taskScheduleStrategy;

    @Setter(onMethod_ = @Inject)
    private MetaTaskScheduler metaTaskScheduler;

    public PlanScheduleTask toPlanScheduleTask(PlanEntity entity, TriggerType triggerType) {
        Plan plan = toPlan(entity);
        // 获取最近一次调度的planInstance和最近一次结束的planInstance
        ScheduleOption scheduleOption = plan.getScheduleOption();
        PlanInstanceEntity latelyTrigger = planInstanceEntityRepo.findLatelyTrigger(entity.getPlanId(), plan.getVersion(), scheduleOption.getScheduleType().type, triggerType.type);
        PlanInstanceEntity latelyFeedback = planInstanceEntityRepo.findLatelyFeedback(entity.getPlanId(), plan.getVersion(), scheduleOption.getScheduleType().type, triggerType.type);

        LocalDateTime latelyTriggerAt = latelyTrigger == null || latelyTrigger.getTriggerAt() == null ? null : latelyTrigger.getTriggerAt().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime latelyFeedbackAt = latelyFeedback == null || latelyFeedback.getFeedbackAt() == null ? null : latelyFeedback.getFeedbackAt().truncatedTo(ChronoUnit.SECONDS);

        return new PlanScheduleTask(
                plan,
                latelyTriggerAt,
                latelyFeedbackAt,
                planScheduleStrategy,
                metaTaskScheduler
        );

    }

    public Plan toPlan(PlanEntity entity) {
        // 获取plan 的当前版本
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(entity.getCurrentVersion())
                .orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INFO + entity.getCurrentVersion()));

        Plan plan;
        PlanType planType = PlanType.parse(planInfoEntity.getPlanType());
        if (PlanType.SINGLE == planType) {
            plan = new SinglePlan(
                    planInfoEntity.getPlanId(),
                    planInfoEntity.getPlanInfoId(),
                    TriggerType.parse(planInfoEntity.getTriggerType()),
                    toScheduleOption(planInfoEntity),
                    JacksonUtils.parseObject(planInfoEntity.getJobInfo(), JobInfo.class)
            );
        } else if (PlanType.WORKFLOW == planType) {
            plan = new WorkflowPlan(
                    planInfoEntity.getPlanId(),
                    planInfoEntity.getPlanInfoId(),
                    TriggerType.parse(planInfoEntity.getTriggerType()),
                    toScheduleOption(planInfoEntity),
                    toJobDag(planInfoEntity.getJobInfo())
            );
        } else {
            throw new IllegalArgumentException("Illegal PlanType in plan:" + entity.getPlanId() + " version:" + entity.getCurrentVersion());
        }
        return plan;
    }

    public static ScheduleOption toScheduleOption(PlanInfoEntity entity) {
        return new ScheduleOption(
                ScheduleType.parse(entity.getScheduleType()),
                entity.getScheduleStartAt(),
                Duration.ofMillis(entity.getScheduleDelay()),
                Duration.ofMillis(entity.getScheduleInterval()),
                entity.getScheduleCron(),
                entity.getScheduleCronType()
        );
    }

    /**
     * @param dag 节点关系
     * @return job dag
     */
    public static DAG<WorkflowJobInfo> toJobDag(String dag) {
        List<WorkflowJobInfo> jobInfos = JacksonUtils.parseObject(dag, new TypeReference<List<WorkflowJobInfo>>() {
        });
        return new DAG<>(jobInfos);
    }

    public static TaskEntity toTaskEntity(Task task) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setJobInstanceId(task.getJobInstanceId());
        taskEntity.setJobId(task.getJobId());
        taskEntity.setPlanId(task.getPlanId());
        taskEntity.setPlanInfoId(task.getPlanVersion());
        taskEntity.setPlanInstanceId(task.getPlanInstanceId());
        taskEntity.setType(task.getType().type);
        taskEntity.setStatus(task.getStatus().status);
        taskEntity.setWorkerId(task.getWorkerId());
        taskEntity.setExecutorName(task.getExecutorName());
        taskEntity.setJobAttributes(task.getJobAttributes() == null ? JacksonUtils.DEFAULT_NONE_OBJECT : task.getJobAttributes().toString());
        taskEntity.setTaskAttributes(JacksonUtils.toJSONString(task.getTaskAttributes()));
        taskEntity.setDispatchOption(JacksonUtils.toJSONString(task.getDispatchOption()));
        taskEntity.setTaskId(task.getTaskId());
        return taskEntity;
    }

    public static Task toTask(TaskEntity entity) {
        if (entity == null) {
            return null;
        }
        TaskType type = TaskType.parse(entity.getType());
        Task task = new Task();
        task.setTaskId(entity.getTaskId());
        task.setJobInstanceId(entity.getJobInstanceId());
        task.setJobId(entity.getJobId());
        task.setStatus(TaskStatus.parse(entity.getStatus()));
        task.setType(type);
        task.setWorkerId(entity.getWorkerId());
        task.setExecutorName(entity.getExecutorName());
        task.setJobAttributes(new Attributes(entity.getJobAttributes()));
        task.setTaskAttributes(type, entity.getTaskAttributes());
        task.setPlanId(entity.getPlanId());
        task.setPlanInstanceId(entity.getPlanInstanceId());
        task.setPlanVersion(entity.getPlanInfoId());
        task.setDispatchOption(JacksonUtils.parseObject(entity.getDispatchOption(), DispatchOption.class));
        return task;
    }

    public TaskScheduleTask toTaskScheduleTask(Task task, LocalDateTime triggerAt) {
        return new TaskScheduleTask(task, triggerAt, taskScheduleStrategy);
    }

    public TaskScheduleTask toTaskScheduleTask(TaskEntity entity) {
        Task task = toTask(entity);
        return new TaskScheduleTask(task, TimeUtils.currentLocalDateTime(), taskScheduleStrategy);
    }

    public static JobInstanceEntity toJobInstanceEntity(JobInstance jobInstance) {
        JobInfo jobInfo = jobInstance.getJobInfo();
        JobInstanceEntity entity = new JobInstanceEntity();
        entity.setJobId(jobInfo.getId());
        entity.setJobInstanceId(jobInstance.getJobInstanceId());
        entity.setPlanInstanceId(jobInstance.getPlanInstanceId());
        entity.setPlanId(jobInstance.getPlanId());
        entity.setPlanInfoId(jobInstance.getPlanVersion());
        entity.setStatus(jobInstance.getStatus().status);
        entity.setContext(jobInstance.getContext().toString());
        entity.setTriggerAt(jobInstance.getTriggerAt());
        entity.setStartAt(jobInstance.getStartAt());
        entity.setEndAt(jobInstance.getEndAt());
        entity.setTerminateWithFail(jobInstance.isTerminateWithFail());
        return entity;
    }

}
