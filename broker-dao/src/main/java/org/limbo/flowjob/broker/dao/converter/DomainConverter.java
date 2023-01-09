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
import org.limbo.flowjob.broker.core.dispatch.DispatchOption;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.SinglePlan;
import org.limbo.flowjob.broker.core.domain.plan.WorkflowPlan;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.PlanScheduleTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.TaskScheduleTask;
import org.limbo.flowjob.broker.core.schedule.strategy.ScheduleStrategyFactory;
import org.limbo.flowjob.broker.dao.entity.JobInfoEntity;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInfoEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.constants.JobStatus;
import org.limbo.flowjob.common.constants.JobType;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.ScheduleType;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基础信息转换 静态方法
 *
 * @author Devil
 * @since 2022/8/11
 */
@Component
public class DomainConverter {

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private JobInfoEntityRepo jobInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private ScheduleStrategyFactory scheduleStrategyFactory;

    @Setter(onMethod_ = @Inject)
    private MetaTaskScheduler metaTaskScheduler;

    public PlanScheduleTask toPlanScheduleTask(PlanEntity entity) {
        // 获取plan 的当前版本
        PlanInfoEntity planInfoEntity = planInfoEntityRepo.findById(entity.getCurrentVersion()).orElse(null);
        Verifies.notNull(planInfoEntity, "does not find " + entity.getPlanId() + " plan's info by version--" + entity.getCurrentVersion() + "");

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
            List<JobInfoEntity> jobInfoEntities = jobInfoEntityRepo.findByPlanInfoId(planInfoEntity.getPlanInfoId());
            Verifies.notEmpty(jobInfoEntities, "does not find " + entity.getPlanId() + " plan's job info by version--" + entity.getCurrentVersion() + "");
            plan = new WorkflowPlan(
                    planInfoEntity.getPlanId(),
                    planInfoEntity.getPlanInfoId(),
                    TriggerType.parse(planInfoEntity.getTriggerType()),
                    toScheduleOption(planInfoEntity),
                    toJobDag(planInfoEntity.getJobInfo(), jobInfoEntities)
            );
        } else {
            throw new IllegalArgumentException("Illegal PlanType in plan:" + entity.getPlanId() + " version:" + entity.getCurrentVersion());
        }

        // 获取最近一次调度的planInstance和最近一次结束的planInstance
        PlanInstanceEntity latelyTrigger = planInstanceEntityRepo.findLatelyTrigger(entity.getPlanId());
        PlanInstanceEntity latelyFeedback = planInstanceEntityRepo.findLatelyFeedback(entity.getPlanId());

        return new PlanScheduleTask(
                plan,
                latelyTrigger == null ? null : latelyTrigger.getTriggerAt(),
                latelyFeedback == null ? null : latelyFeedback.getFeedbackAt(),
                scheduleStrategyFactory.build(plan.planType()),
                metaTaskScheduler
        );

    }

    public ScheduleOption toScheduleOption(PlanInfoEntity entity) {
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
    public DAG<JobInfo> toJobDag(String dag, List<JobInfoEntity> jobInfoEntities) {
        List<JobInfo> jobInfos = JacksonUtils.parseObject(dag, new TypeReference<List<JobInfo>>() {
        });
        Map<String, JobInfoEntity> jobInfoEntityMap = jobInfoEntities.stream().collect(Collectors.toMap(JobInfoEntity::getName, entity -> entity, (entity, entity2) -> entity));
        for (JobInfo jobInfo : jobInfos) {
            JobInfoEntity jobInfoEntity = jobInfoEntityMap.get(jobInfo.getName());
            jobInfo.setDescription(jobInfoEntity.getDescription());
            jobInfo.setTriggerType(TriggerType.parse(jobInfoEntity.getTriggerType()));
            jobInfo.setType(JobType.parse(jobInfoEntity.getType()));
            jobInfo.setAttributes(new Attributes(jobInfoEntity.getAttributes()));
            jobInfo.setDispatchOption(JacksonUtils.parseObject(jobInfoEntity.getDispatchOption(), DispatchOption.class));
            jobInfo.setExecutorName(jobInfoEntity.getExecutorName());
            jobInfo.setTerminateWithFail(jobInfoEntity.getTerminateWithFail());
        }
        return new DAG<>(jobInfos);
    }

    public JobInstance toJobInstance(JobInstanceEntity entity, JobInfoEntity jobInfo) {
        JobInstance jobInstance = new JobInstance();
        jobInstance.setJobInstanceId(entity.getJobInstanceId());
        jobInstance.setPlanInstanceId(entity.getPlanInstanceId());
        jobInstance.setPlanVersion(entity.getPlanInfoId());
        jobInstance.setPlanId(entity.getPlanId());
        jobInstance.setJobId(entity.getJobId());
        jobInstance.setStatus(JobStatus.parse(entity.getStatus()));
        jobInstance.setStartAt(entity.getStartAt());
        jobInstance.setEndAt(entity.getEndAt());
        jobInstance.setAttributes(new Attributes(entity.getAttributes()));
        jobInstance.setTriggerAt(entity.getTriggerAt());
        jobInstance.setDispatchOption(JacksonUtils.parseObject(jobInfo.getDispatchOption(), DispatchOption.class));
        jobInstance.setExecutorName(jobInfo.getExecutorName());
        jobInstance.setType(JobType.parse(jobInfo.getType()));
        jobInstance.setTerminateWithFail(jobInfo.getTerminateWithFail());
        return jobInstance;
    }

    public TaskEntity toTaskEntity(Task task, LocalDateTime triggerAt) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setJobInstanceId(task.getJobInstanceId());
        taskEntity.setJobId(task.getJobId());
        taskEntity.setPlanId(task.getPlanId());
        taskEntity.setPlanInfoId(task.getPlanVersion());
        taskEntity.setType(task.getTaskType().type);
        taskEntity.setStatus(task.getStatus().status);
        taskEntity.setWorkerId(task.getWorkerId());
        taskEntity.setAttributes(task.getAttributes() == null ? "{}" : task.getAttributes().toString());
        taskEntity.setTaskId(task.getTaskId());
        taskEntity.setTriggerAt(triggerAt);
        return taskEntity;
    }

    public Task toTask(TaskEntity entity) {
        Task task = new Task();
        task.setTaskId(entity.getTaskId());
        task.setJobInstanceId(entity.getJobInstanceId());
        task.setJobId(entity.getJobId());
        task.setStatus(TaskStatus.parse(entity.getStatus()));
        task.setWorkerId(entity.getWorkerId());
        task.setAttributes(new Attributes(entity.getAttributes()));
        task.setPlanId(entity.getPlanId());
        task.setPlanVersion(entity.getPlanInfoId());

        // job todo @pq
        JobInfoEntity jobInfoEntity = jobInfoEntityRepo.findByPlanInfoIdAndName(entity.getPlanInfoId(), entity.getJobId());
        task.setDispatchOption(JacksonUtils.parseObject(jobInfoEntity.getDispatchOption(), DispatchOption.class));
        task.setExecutorName(jobInfoEntity.getExecutorName());
        return task;
    }

    public TaskScheduleTask toTaskScheduleTask(Task task, LocalDateTime triggerAt) {
        return new TaskScheduleTask(task, triggerAt, scheduleStrategyFactory.build(task.getPlanType()));
    }

    public TaskScheduleTask toTaskScheduleTask(TaskEntity entity) {
        Task task = toTask(entity);
        return new TaskScheduleTask(task, entity.getTriggerAt(), scheduleStrategyFactory.build(task.getPlanType()));
    }

    public JobInstanceEntity toJobInstanceEntity(JobInstance jobInstance) {
        JobInstanceEntity entity = new JobInstanceEntity();
        entity.setJobInstanceId(jobInstance.getJobInstanceId());
        entity.setPlanInstanceId(jobInstance.getPlanInstanceId());
        entity.setPlanId(jobInstance.getPlanId());
        entity.setPlanInfoId(jobInstance.getPlanVersion());
        entity.setJobId(jobInstance.getJobId());
        entity.setStatus(jobInstance.getStatus().status);
        entity.setAttributes(JacksonUtils.toJSONString(jobInstance.getAttributes(), JacksonUtils.DEFAULT_NONE_OBJECT));
        entity.setTriggerAt(jobInstance.getTriggerAt());
        entity.setStartAt(jobInstance.getStartAt());
        entity.setEndAt(jobInstance.getEndAt());
        return entity;
    }

}
