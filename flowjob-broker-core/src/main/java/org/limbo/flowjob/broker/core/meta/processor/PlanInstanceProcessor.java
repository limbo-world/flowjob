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

package org.limbo.flowjob.broker.core.meta.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.ExecuteResult;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.ScheduleType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.api.param.broker.JobFeedbackParam;
import org.limbo.flowjob.broker.core.agent.AgentRegistry;
import org.limbo.flowjob.broker.core.cluster.Node;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.meta.IDGenerator;
import org.limbo.flowjob.broker.core.meta.IDType;
import org.limbo.flowjob.broker.core.meta.info.Plan;
import org.limbo.flowjob.broker.core.meta.info.PlanRepository;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.meta.instance.Instance;
import org.limbo.flowjob.broker.core.meta.instance.InstanceFactory;
import org.limbo.flowjob.broker.core.meta.instance.PlanInstance;
import org.limbo.flowjob.broker.core.meta.instance.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.meta.job.JobInstance;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceFactory;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.meta.task.JobInstanceTask;
import org.limbo.flowjob.broker.core.meta.task.PlanScheduleTask;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.service.TransactionService;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 处理整体调度的逻辑
 *
 * @author Devil
 * @since 2023/12/7
 */
@Slf4j
public class PlanInstanceProcessor extends InstanceProcessor {

    private final PlanRepository planRepository;

    private final PlanInstanceRepository planInstanceRepository;

    public PlanInstanceProcessor(MetaTaskScheduler metaTaskScheduler,
                                 IDGenerator idGenerator,
                                 NodeManger nodeManger,
                                 AgentRegistry agentRegistry,
                                 PlanRepository planRepository,
                                 TransactionService transactionService,
                                 PlanInstanceRepository planInstanceRepository,
                                 JobInstanceRepository jobInstanceRepository) {
        super(agentRegistry, nodeManger, idGenerator, metaTaskScheduler, transactionService, jobInstanceRepository);
        this.planRepository = planRepository;
        this.planInstanceRepository = planInstanceRepository;
    }

    // 如是定时1小时后执行，task的创建问题 比如任务执行失败后，重试间隔可能导致这个问题
    // 比如广播模式下，一小时后的节点数和当前的肯定是不同的
    public String schedule(Plan plan, TriggerType triggerType, Attributes attributes, LocalDateTime triggerAt) {
        ScheduleContext scheduleContext = new ScheduleContext();
        String instanceId = transactionService.transactional(() -> {

            String planId = plan.getId();
            String version = plan.getVersion();

            // 悲观锁快速释放，不阻塞后续任务
            Plan currentPlan = planRepository.lockAndGet(plan.getId());

            Verifies.notNull(currentPlan, MessageFormat.format("plan:{0} is null", planId));

            // 判断任务配置信息是否变动：任务是由之前时间创建的 调度时候如果版本改变 可能会有调度时间的变化本次就无需执行
            // 比如 5s 执行一次 分别在 5s 10s 15s 在11s的时候内存里下次执行为 15s 此时修改为 2s 执行一次 那么重新加载plan后应该为 12s 14s 所以15s这次可以跳过
            Verifies.verify(Objects.equals(version, currentPlan.getVersion()), MessageFormat.format("plan:{0} version {1} change to {2}", planId, version, currentPlan.getVersion()));

            ScheduleOption scheduleOption = currentPlan.getScheduleOption();

            // 判断是否由当前节点执行
            if (TriggerType.API != triggerType) {

                Verifies.verify(currentPlan.isEnabled(), MessageFormat.format("plan:{0} is not enabled", planId));

                // 校验是否重复创建
                PlanInstance latelyPlanInstance = planInstanceRepository.getLatelyTrigger(planId, version, currentPlan.getScheduleOption().getScheduleType(), triggerType);
                switch (scheduleOption.getScheduleType()) {
                    case FIXED_RATE:
                    case CRON:
                        if (!(latelyPlanInstance == null || !triggerAt.isEqual(latelyPlanInstance.getTriggerAt()))) {
                            throw new VerifyException(MessageFormat.format("Duplicate create PlanInstance,triggerAt:{0} planId[{1}] Version[{2}] oldPlanInstance[{3}]",
                                    triggerAt, planId, version, latelyPlanInstance.getId()));
                        }
                        break;
                    case FIXED_DELAY:
                        if (!(latelyPlanInstance == null || (!triggerAt.isEqual(latelyPlanInstance.getTriggerAt()) && latelyPlanInstance.getStatus().isCompleted()))) {
                            throw new VerifyException(MessageFormat.format("Please wait last PlanInstance[{0}] complete.Plan[{1}] Version[{2}]",
                                    latelyPlanInstance.getId(), planId, version));
                        }
                        break;
                    default:
                        throw new VerifyException(MsgConstants.UNKNOWN + " scheduleType:" + currentPlan.getScheduleOption().getScheduleType());
                }
            }

            String id = idGenerator.generateId(IDType.INSTANCE);
            PlanInstance planInstance = InstanceFactory.create(id, currentPlan, attributes, triggerAt);

            PlanInstance existPlanInstance = planInstanceRepository.get(planInstance.getId());
            Verifies.isNull(existPlanInstance, MessageFormat.format("plan:{0} version {1} create instance by id {2} but is already exist", planId, version, planInstance.getId()));

            planInstanceRepository.save(planInstance);

            // 获取头部节点
            List<JobInstance> jobInstances = new ArrayList<>();
            for (WorkflowJobInfo jobInfo : plan.getDag().origins()) {
                if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                    String jobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
                    Node elect = nodeManger.elect(jobInstanceId);
                    jobInstances.add(JobInstanceFactory.create(jobInstanceId, planInstance.getId(), planInstance.getType(), elect.getUrl(), attributes, new Attributes(), jobInfo, triggerAt));
                }
            }
            jobInstanceRepository.saveAll(jobInstances);
            scheduleContext.setWaitScheduleJobs(jobInstances);
            return id;
        });

        asyncSchedule(scheduleContext);
        return instanceId;
    }

    /**
     * api 方式下发节点任务
     */
    public String scheduleJob(String planInstanceId, String jobId) {
        PlanInstance planInstance = planInstanceRepository.get(planInstanceId);
        Verifies.notNull(planInstance, MsgConstants.CANT_FIND_PLAN_INSTANCE + planInstanceId);
        ScheduleContext scheduleContext = new ScheduleContext();
        String jobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
        transactionService.transactional(() -> {
            DAG<WorkflowJobInfo> dag = planInstance.getDag();
            WorkflowJobInfo jobInfo = dag.getNode(jobId);

            Verifies.verify(checkJobsSuccess(planInstanceId, dag.preNodes(jobInfo.getId()), true), "previous job is not complete, please wait!");

            Verifies.verify(TriggerType.API == jobInfo.getTriggerType(), "only api triggerType job can schedule by api");

            Node elect = nodeManger.elect(jobInstanceId);
            JobInstance jobInstance = JobInstanceFactory.create(jobInstanceId, planInstanceId, planInstance.getType(), elect.getUrl(), planInstance.getAttributes(), new Attributes(), jobInfo, TimeUtils.currentLocalDateTime());
            jobInstanceRepository.save(jobInstance);
            scheduleContext.setWaitScheduleJobs(Collections.singletonList(jobInstance));
            return jobInstance;
        });
        asyncSchedule(scheduleContext);
        return jobInstanceId;
    }

    @Override
    protected boolean instanceExecuting(String instanceId) {
        return planInstanceRepository.executing(instanceId, TimeUtils.currentLocalDateTime());
    }

    /**
     * 任务执行反馈
     *
     * @param jobInstanceId Id
     * @param param         反馈参数
     */
    public void feedback(String jobInstanceId, JobFeedbackParam param) {
        ExecuteResult result = param.getResult();
        if (log.isDebugEnabled()) {
            log.debug("receive job feedback id:{} result:{}", jobInstanceId, result);
        }

        JobInstance jobInstance = jobInstanceRepository.get(jobInstanceId);
        Verifies.notNull(jobInstance, "job instance is null id:" + jobInstanceId);
        ScheduleContext scheduleContext = transactionService.transactional(() -> {
            switch (result) {
                case SUCCEED:
                    return handleJobSuccess(jobInstance);

                case FAILED:
                    return handleJobFail(jobInstance, param.getErrorMsg());

                case TERMINATED:
                    throw new UnsupportedOperationException("暂不支持手动终止任务");

                default:
                    throw new IllegalStateException("Unexpect execute result: " + param.getResult());
            }
        });

        asyncSchedule(scheduleContext);
    }

    /**
     * 手工下发 job
     * todo 执行的时候可以选择 是就只重新计算当前的节点还是后续节点是否也重新执行一遍
     */
    public void manualScheduleJob(String instanceId, String jobId) {
        PlanInstance planInstance = planInstanceRepository.get(instanceId);
        Verifies.notNull(planInstance, MsgConstants.CANT_FIND_PLAN_INSTANCE + instanceId);
        ScheduleContext scheduleContext = new ScheduleContext();
        transactionService.transactional(() -> {
            DAG<WorkflowJobInfo> dag = planInstance.getDag();
            WorkflowJobInfo jobInfo = dag.getNode(jobId);

            Verifies.verify(checkJobsSuccess(instanceId, dag.preNodes(jobInfo.getId()), true), "previous job is not complete, please wait!");

            JobInstance jobInstance = jobInstanceRepository.getLatest(instanceId, jobId);// 获取最后一条
            String newJobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
            jobInstance.retryReset(newJobInstanceId, 0);
            jobInstanceRepository.save(jobInstance);
            scheduleContext.setWaitScheduleJobs(Collections.singletonList(jobInstance));
            return jobInstance;
        });
        asyncSchedule(scheduleContext);
    }

    @Override
    protected void asyncSchedule(ScheduleContext scheduleContext) {
        if (scheduleContext == null) {
            return;
        }
        if (scheduleContext.getWaitSchedulePlan() != null) {
            PlanScheduleTask metaTask = new PlanScheduleTask(scheduleContext.getWaitSchedulePlan(), this, metaTaskScheduler);
            metaTaskScheduler.schedule(metaTask);
        }
        if (CollectionUtils.isNotEmpty(scheduleContext.getWaitScheduleJobs())) {
            for (JobInstance jobInstance : scheduleContext.getWaitScheduleJobs()) {
                JobInstanceTask metaTask = new JobInstanceTask(jobInstance, agentRegistry);
                metaTaskScheduler.schedule(metaTask);
            }
        }
    }

    @Override
    protected void handlerInstanceComplete(String instanceId, boolean success, ScheduleContext scheduleContext) {
        PlanInstance planInstance = planInstanceRepository.get(instanceId);
        Verifies.notNull(planInstance, MsgConstants.CANT_FIND_PLAN_INSTANCE + instanceId);
        if (success) {
            planInstanceRepository.success(instanceId, TimeUtils.currentLocalDateTime());
        } else {
            LocalDateTime current = TimeUtils.currentLocalDateTime();
            LocalDateTime startAt = planInstance.getStartAt() == null ? current : planInstance.getStartAt();
            planInstanceRepository.fail(instanceId, startAt, current);
        }
        // 下发 fixed_delay 任务
        if (ScheduleType.FIXED_DELAY == planInstance.getScheduleType()) {
            Plan plan = planRepository.get(planInstance.getPlanId());
            scheduleContext.setWaitSchedulePlan(plan);
        }
    }

    @Override
    protected Instance lockAndGet(String instanceId) {
        return planInstanceRepository.lockAndGet(instanceId);
    }

}
