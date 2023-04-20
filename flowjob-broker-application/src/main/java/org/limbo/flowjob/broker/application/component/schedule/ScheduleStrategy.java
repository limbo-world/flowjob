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

package org.limbo.flowjob.broker.application.component.schedule;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.domain.plan.SinglePlan;
import org.limbo.flowjob.broker.core.domain.plan.WorkflowPlan;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.TaskScheduleTask;
import org.limbo.flowjob.broker.core.schedule.strategy.IPlanScheduleStrategy;
import org.limbo.flowjob.broker.core.schedule.strategy.ITaskResultStrategy;
import org.limbo.flowjob.broker.core.schedule.strategy.ITaskScheduleStrategy;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.PlanType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.exception.VerifyException;
import org.limbo.flowjob.common.utils.Verifies;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Devil
 * @since 2023/2/8
 */
@Slf4j
@Component
public class ScheduleStrategy implements IPlanScheduleStrategy, ITaskScheduleStrategy, ITaskResultStrategy {

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    private DomainConverter domainConverter;

    @Setter(onMethod_ = @Inject)
    private JobInstanceHelper jobInstanceHelper;

    @Setter(onMethod_ = @Inject)
    private MetaTaskScheduler metaTaskScheduler;

    @Setter(onMethod_ = @Inject)
    private ScheduleStrategyHelper scheduleStrategyHelper;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;

    @Setter(onMethod_ = @Inject)
    private IDGenerator idGenerator;

    @Override
    public void schedule(TriggerType triggerType, Plan plan, LocalDateTime triggerAt) {
        executeWithAspect(unused -> {
            // 悲观锁快速释放，不阻塞后续任务
            String planInstanceId = idGenerator.generateId(IDType.PLAN_INSTANCE);
            PlanInstance planInstance = PlanInstance.builder()
                    .planInstanceId(planInstanceId)
                    .triggerType(triggerType)
                    .plan(plan)
                    .triggerAt(triggerAt)
                    .build();
            planInstanceRepository.save(planInstance);

            // 调度逻辑
            List<JobInstance> jobInstances = newJobInstancesByPlan(planInstance, triggerAt);
            if (CollectionUtils.isNotEmpty(jobInstances)) {
                scheduleStrategyHelper.saveAndScheduleJobInstances(jobInstances, triggerAt);
            }
        });
    }

    /**
     * 调度 PlanInstance
     */
    public void schedulePlanInstance(PlanInstance planInstance) {
        executeWithAspect(unused -> {
            LocalDateTime triggerAt = planInstance.getTriggerAt();
            List<JobInstance> jobInstances = newJobInstancesByPlan(planInstance, triggerAt);
            if (CollectionUtils.isNotEmpty(jobInstances)) {
                scheduleStrategyHelper.saveAndScheduleJobInstances(jobInstances, triggerAt);
            }
        });
    }

    private List<JobInstance> newJobInstancesByPlan(PlanInstance planInstance, LocalDateTime triggerAt) {
        Plan plan = planInstance.getPlan();
        String planInstanceId = planInstance.getPlanInstanceId();
        List<JobInstance> jobInstances = new ArrayList<>();
        if (PlanType.SINGLE == plan.getType()) {
            JobInfo jobInfo = ((SinglePlan) plan).getJobInfo();
            JobInstance jobInstance = jobInstanceHelper.newJobInstance(plan.getPlanId(), plan.getVersion(), PlanType.SINGLE, planInstanceId, new Attributes(), jobInfo, triggerAt);
            jobInstances.add(jobInstance);

        } else {

            // 获取头部节点
            for (WorkflowJobInfo jobInfo : ((WorkflowPlan) plan).getDag().origins()) {
                if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                    jobInstances.add(jobInstanceHelper.newJobInstance(plan.getPlanId(), plan.getVersion(), PlanType.WORKFLOW, planInstanceId, new Attributes(), jobInfo, triggerAt));
                }
            }
        }
        return jobInstances;
    }

    /**
     * api 方式下发节点任务
     */
    public void apiScheduleJob(String planId, String planInstanceId, String jobId) {
        executeWithAspect(unused -> {
            PlanEntity planEntity = planEntityRepo.findById(planId).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN + planId));

            Plan plan = domainConverter.toPlan(planEntity);
            Verifies.verify(PlanType.WORKFLOW == plan.getType(), "only workflow plan can schedule by api");

            WorkflowPlan workflowPlan = (WorkflowPlan) plan;
            DAG<WorkflowJobInfo> dag = workflowPlan.getDag();
            WorkflowJobInfo jobInfo = dag.getNode(jobId);

            // todo 无法重新调度 需要判断是重试还是触发调度中的任务
            Verifies.verify(TriggerType.API == jobInfo.getTriggerType(), "only api triggerType job can schedule by api");

            LocalDateTime triggerAt = TimeUtils.currentLocalDateTime();

            Verifies.verify(scheduleStrategyHelper.checkJobsSuccess(planInstanceId, dag.preNodes(jobInfo.getId()), true), "previous job is not complete, please wait!");

            JobInstance jobInstance = scheduleStrategyHelper.lockAndSaveJobInstance(plan, planInstanceId, jobInfo, triggerAt);

            // 前置节点已经完成则可以下发
            scheduleStrategyHelper.scheduleJobInstances(Collections.singletonList(jobInstance), triggerAt);
        });
    }

    @Override
    public void handleSuccess(Task task, Object result) {
        executeWithAspect(unused -> scheduleStrategyHelper.handleSuccess(task, result));
    }

    @Override
    public void handleFail(Task task, String errorMsg, String errorStackTrace) {
        executeWithAspect(unused -> scheduleStrategyHelper.handleFail(task, errorMsg, errorStackTrace));
    }

    @Override
    public void schedule(Task task) {
        executeWithAspect(unused -> scheduleStrategyHelper.schedule(task));
    }

    public void executeWithAspect(Consumer<Void> consumer) {
        try {
            // new context
            ScheduleStrategyContext.set();
            // do real
            consumer.accept(null);
            // do after
            scheduleTasks();
        } finally {
            // clear context
            ScheduleStrategyContext.clear();
        }
    }

    /**
     * 放在事务外，防止下发和执行很快但是task下发完需要很久的情况，这样前面的任务执行返回后由于事务未提交，会提示找不到task
     */
    public void scheduleTasks() {
        if (CollectionUtils.isEmpty(ScheduleStrategyContext.waitScheduleTasks())) {
            return;
        }
        for (TaskScheduleTask task : ScheduleStrategyContext.waitScheduleTasks()) {
            try {
                metaTaskScheduler.schedule(task);
            } catch (Exception e) {
                // 由task的状态检查任务去修复task的执行情况
                log.error("task schedule fail! task={}", task, e);
            }
        }
    }

}
