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

package org.limbo.flowjob.broker.application.schedule;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.ScheduleType;
import org.limbo.flowjob.api.constants.TaskStatus;
import org.limbo.flowjob.api.constants.TaskType;
import org.limbo.flowjob.broker.core.dispatch.TaskDispatcher;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.domain.task.TaskFactory;
import org.limbo.flowjob.broker.core.exceptions.JobException;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/5/8
 */
@Slf4j
public abstract class AbstractPlanScheduler implements PlanScheduler {

    @Setter(onMethod_ = @Inject)
    protected TaskFactory taskFactory;

    @Setter(onMethod_ = @Inject)
    protected JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    protected TaskEntityRepo taskEntityRepo;

    @Setter(onMethod_ = @Inject)
    protected JobInstanceRepository jobInstanceRepository;

    @Setter(onMethod_ = @Inject)
    protected PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    protected PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    protected IDGenerator idGenerator;

    @Setter(onMethod_ = @Inject)
    protected TaskDispatcher taskDispatcher;

    @Setter(onMethod_ = @Inject)
    protected MetaTaskScheduler metaTaskScheduler;

    @Transactional
    public void schedule(Plan plan, String planInstanceId, LocalDateTime triggerAt) {
        List<JobInstance> jobInstances = createJobInstances(plan, planInstanceId, triggerAt);
        saveAndScheduleJobInstances(jobInstances);
    }

    // 如是定时1小时后执行，task的创建问题 比如任务执行失败后，重试间隔可能导致这个问题
    // 比如广播模式下，一小时后的节点数和当前的肯定是不同的
    protected void saveAndScheduleJobInstances(List<JobInstance> jobInstances) {
        saveJobInstances(jobInstances);
        ScheduleContext.waitScheduleJobs(jobInstances);
    }

    public abstract List<JobInstance> createJobInstances(Plan plan, String planInstanceId, LocalDateTime triggerAt);


    @Override
    @Transactional
    public void schedule(JobInstance jobInstance) {
        // 根据job类型创建task
        List<Task> tasks;
        JobInfo jobInfo = jobInstance.getJobInfo();
        switch (jobInfo.getType()) {
            case NORMAL:
                tasks = taskFactory.create(jobInstance, TaskType.NORMAL);
                break;
            case BROADCAST:
                tasks = taskFactory.create(jobInstance, TaskType.BROADCAST);
                break;
            case MAP:
            case MAP_REDUCE:
                tasks = taskFactory.create(jobInstance, TaskType.SPLIT);
                break;
            default:
                throw new JobException(jobInfo.getId(), MsgConstants.UNKNOWN + " job type:" + jobInfo.getType().type);
        }

        // 如果可以创建的任务为空（只有广播任务）
        if (CollectionUtils.isEmpty(tasks)) {
            handleJobSuccess(jobInstance);
        } else {
            saveTasks(tasks);
        }

    }

    @Transactional
    public void saveJobInstances(List<JobInstance> jobInstances) {
        if (CollectionUtils.isEmpty(jobInstances)) {
            return;
        }

        // 保存 jobInstance
        List<JobInstanceEntity> jobInstanceEntities = jobInstances.stream().map(DomainConverter::toJobInstanceEntity).collect(Collectors.toList());
        jobInstanceEntityRepo.saveAll(jobInstanceEntities);
        jobInstanceEntityRepo.flush();
    }

    @Transactional
    public void handleSuccess(Task task, Object result) {
        int num = taskEntityRepo.success(task.getTaskId(), TimeUtils.currentLocalDateTime(),
                task.getContext().toString(), task.getJobAttributes().toString(), JacksonUtils.toJSONString(result)
        );

        if (num != 1) { // 已经被更新 无需重复处理
            return;
        }

        JobInstance jobInstance = jobInstanceRepository.get(task.getJobInstanceId());
        if (JobStatus.EXECUTING != jobInstance.getStatus()) {
            log.warn("task:{} update status success but jobInstance:{} is already changed", task.getTaskId(), task.getJobInstanceId());
            return;
        }
        // 检查task是否都已经完成
        List<TaskEntity> taskEntities = taskEntityRepo.findByJobInstanceIdAndType(task.getJobInstanceId(), task.getType().type);
        boolean success = taskEntities.stream().allMatch(entity -> TaskStatus.SUCCEED == TaskStatus.parse(entity.getStatus()));
        if (!success) {
            return; // 交由失败的task 或者后面还在执行的task去做后续逻辑处理
        }
        // 聚合上下文内容
        Attributes context = new Attributes();
        for (TaskEntity taskEntity : taskEntities) {
            Attributes taskContext = new Attributes(taskEntity.getContext());
            context.put(taskContext);
        }
        jobInstance.setContext(context);
        // 聚合job参数
        Attributes jobAttributes = jobInstance.getJobAttributes();
        for (TaskEntity taskEntity : taskEntities) {
            Attributes taskJobAttrs = new Attributes(taskEntity.getJobAttributes());
            jobAttributes.put(taskJobAttrs);
        }
        // 判断当前 job 类型 进行后续处理
        JobInfo jobInfo = jobInstance.getJobInfo();
        switch (jobInfo.getType()) {
            case NORMAL:
            case BROADCAST:
                handleJobSuccess(jobInstance);
                break;
            case MAP:
                handleMapJobSuccess(task, jobInstance);
                break;
            case MAP_REDUCE:
                handleMapReduceJobSuccess(task, jobInstance);
                break;
            default:
                throw new IllegalArgumentException(MsgConstants.UNKNOWN + " JobType in jobInstance:" + jobInstance.getJobInstanceId());
        }
    }

    public abstract void handleJobSuccess(JobInstance jobInstance);

    /**
     * 处理 map job 执行成功
     */
    private void handleMapJobSuccess(Task task, JobInstance jobInstance) {
        switch (task.getType()) {
            case SPLIT:
                handleSplitTaskSuccess(jobInstance);
                break;
            case MAP:
                handleJobSuccess(jobInstance);
                break;
            default:
                throw new IllegalArgumentException("Illegal TaskType in task:" + task.getTaskId());
        }
    }

    /**
     * 处理 map/reduce job 执行成功
     */
    private void handleMapReduceJobSuccess(Task task, JobInstance jobInstance) {
        switch (task.getType()) {
            case SPLIT:
                handleSplitTaskSuccess(jobInstance);
                break;
            case MAP:
                List<Task> tasks = taskFactory.create(jobInstance, TaskType.REDUCE);
                saveTasks(tasks);
                break;
            case REDUCE:
                handleJobSuccess(jobInstance);
                break;
            default:
                throw new IllegalArgumentException("Illegal TaskType in task:" + task.getTaskId());
        }
    }

    /**
     * 处理分片任务
     */
    private void handleSplitTaskSuccess(JobInstance jobInstance) {
        List<Task> tasks = taskFactory.create(jobInstance, TaskType.MAP);
        if (CollectionUtils.isEmpty(tasks)) {
            handleJobSuccess(jobInstance);
        } else {
            saveTasks(tasks);
        }
    }

    @Transactional
    public void saveTasks(List<Task> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }

        List<TaskEntity> taskEntities = tasks.stream().map(DomainConverter::toTaskEntity).collect(Collectors.toList());
        taskEntityRepo.saveAll(taskEntities);
        taskEntityRepo.flush();

        // task保存后才进行下发
        ScheduleContext.waitScheduleTasks(tasks);
    }

    public void handlerPlanComplete(String planInstanceId, boolean success) {
        if (success) {
            planInstanceEntityRepo.success(planInstanceId, TimeUtils.currentLocalDateTime());
        } else {
            planInstanceEntityRepo.fail(planInstanceId, TimeUtils.currentLocalDateTime());
        }
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(planInstanceId).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INSTANCE + planInstanceId));
        if (ScheduleType.FIXED_DELAY == ScheduleType.parse(planInstanceEntity.getScheduleType())) {
            // 如果为 FIXED_DELAY 更新 plan  使得 UpdatedPlanLoadTask 进行重新加载
            planEntityRepo.updateTime(planInstanceEntity.getPlanId(), TimeUtils.currentLocalDateTime());
        }
    }

    public JobInstance createJobInstance(String planId, String planVersion, String planInstanceId,
                                         Attributes context, JobInfo jobInfo, LocalDateTime triggerAt) {
        String jobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
        JobInstance instance = new JobInstance();
        instance.setJobInstanceId(jobInstanceId);
        instance.setJobInfo(jobInfo);
        instance.setPlanType(getPlanType());
        instance.setPlanId(planId);
        instance.setPlanInstanceId(planInstanceId);
        instance.setPlanVersion(planVersion);
        instance.setStatus(JobStatus.SCHEDULING);
        instance.setTriggerAt(triggerAt);
        instance.setContext(context == null ? new Attributes() : context);
        instance.setJobAttributes(jobInfo.getAttributes() == null ? new Attributes() : jobInfo.getAttributes());
        return instance;
    }

}
