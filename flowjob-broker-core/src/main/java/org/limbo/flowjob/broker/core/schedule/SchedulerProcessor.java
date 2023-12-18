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

package org.limbo.flowjob.broker.core.schedule;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.ConstantsPool;
import org.limbo.flowjob.api.constants.ExecuteResult;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.PlanStatus;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.ScheduleType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.api.constants.rpc.HttpAgentApi;
import org.limbo.flowjob.api.param.broker.JobFeedbackParam;
import org.limbo.flowjob.broker.core.agent.AgentRegistry;
import org.limbo.flowjob.broker.core.agent.ScheduleAgent;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.domain.job.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.domain.plan.PlanRepository;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.task.PlanScheduleTask;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;
import org.limbo.flowjob.common.rpc.RPCInvocation;
import org.limbo.flowjob.common.thread.CommonThreadPool;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.json.JacksonUtils;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 处理整体调度的逻辑
 *
 * @author Devil
 * @since 2023/12/7
 */
@Slf4j
public class SchedulerProcessor {

    private MetaTaskScheduler metaTaskScheduler;

    private IDGenerator idGenerator;

    private AgentRegistry agentRegistry;

    private PlanRepository planRepository;

    private PlanInstanceRepository planInstanceRepository;

    private JobInstanceRepository jobInstanceRepository;

    private LBStrategy<ScheduleAgent> lbStrategy;

    public SchedulerProcessor(MetaTaskScheduler metaTaskScheduler,
                              IDGenerator idGenerator,
                              AgentRegistry agentRegistry,
                              PlanRepository planRepository,
                              PlanInstanceRepository planInstanceRepository,
                              JobInstanceRepository jobInstanceRepository) {
        this.metaTaskScheduler = metaTaskScheduler;
        this.idGenerator = idGenerator;
        this.agentRegistry = agentRegistry;
        this.planRepository = planRepository;
        this.planInstanceRepository = planInstanceRepository;
        this.jobInstanceRepository = jobInstanceRepository;
        this.lbStrategy = new RoundRobinLBStrategy<>();
    }

    // 如是定时1小时后执行，task的创建问题 比如任务执行失败后，重试间隔可能导致这个问题
    // 比如广播模式下，一小时后的节点数和当前的肯定是不同的
    public void schedule(String planId, TriggerType triggerType, Attributes planAttributes, LocalDateTime triggerAt) {
        List<JobInstance> jobInstances = schedulePlan(planId, triggerType, planAttributes, triggerAt);
        scheduleJobs(jobInstances);
    }

    // todo @ys 性能优化
    public List<JobInstance> schedulePlan(String planId, TriggerType triggerType, Attributes planAttributes, LocalDateTime triggerAt) {

        Plan plan = planRepository.lockAndGet(planId);

        // 悲观锁快速释放，不阻塞后续任务
        PlanInstance planInstance = createPlanInstance(plan, planAttributes, triggerAt);

        // 判断任务配置信息是否变动：任务是由之前时间创建的 调度时候如果版本改变 可能会有调度时间的变化本次就无需执行
        // 比如 5s 执行一次 分别在 5s 10s 15s 在11s的时候内存里下次执行为 15s 此时修改为 2s 执行一次 那么重新加载plan后应该为 12s 14s 所以15s这次可以跳过
        // todo 这个是不是还需要
        Verifies.verify(Objects.equals(version, planEntity.getCurrentVersion()), MessageFormat.format("plan:{0} version {1} change to {2}", planId, version, planEntity.getCurrentVersion()));

        PlanInstanceEntity planInstanceEntityCheck = planInstanceEntityRepo.findById(planInstanceId).orElse(null);
        Verifies.isNull(planInstanceEntityCheck, MessageFormat.format("plan:{0} version {1} create instance by id {2} but is already exist", planId, version, planInstanceId));

        ScheduleOption scheduleOption = plan.getScheduleOption();

        // 判断是否由当前节点执行
        if (TriggerType.API != triggerType) {

            Verifies.verify(plan.isEnabled(), "plan " + planId + " is not enabled");

            // 校验是否重复创建
            PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findLatelyTrigger(planId, planInfoEntity.getPlanInfoId(), planInfoEntity.getScheduleType(), triggerType.type);
            switch (scheduleOption.getScheduleType()) {
                case FIXED_RATE:
                case CRON:
                    slotManager.checkPlanId(planId); // fixed_delay 可以在不同节点触发
                    Verifies.verify(planInstanceEntity == null || !triggerAt.isEqual(planInstanceEntity.getTriggerAt()),
                            MessageFormat.format("Duplicate create PlanInstance,triggerAt:{0} planId[{1}] Version[{2}] oldPlanInstance[{3}]",
                                    triggerAt, planId, version, JacksonUtils.toJSONString(planInstanceEntity))
                    );
                    break;
                case FIXED_DELAY:
                    Verifies.verify(planInstanceEntity == null || (!triggerAt.isEqual(planInstanceEntity.getTriggerAt()) && PlanStatus.parse(planInstanceEntity.getStatus()).isCompleted()),
                            MessageFormat.format("Please wait last PlanInstance[{0}] complete.Plan[{1}] Version[{2}]",
                                    JacksonUtils.toJSONString(planInstanceEntity), planId, version)
                    );
                    break;
                default:
                    throw new VerifyException(MsgConstants.UNKNOWN + " scheduleType:" + plan.getScheduleOption().getScheduleType());
            }
        }



        planInstanceRepository.save(planInstance);
        List<JobInstance> jobInstances = createScheduleJobInstances(plan, planAttributes, planInstance.getId(), triggerAt);
        jobInstanceRepository.saveAll(jobInstances);
        return jobInstances;
    }

    /**
     * api 方式下发节点任务
     */
    public void scheduleJob(String planInstanceId, String jobId) {
        PlanInstance planInstance = planInstanceRepository.get(planInstanceId);
        Verifies.notNull(planInstance, MsgConstants.CANT_FIND_PLAN_INSTANCE + planInstanceId);
        Plan plan = planRepository.getByVersion(planInstance.getPlanId(), planInstance.getVersion());
        List<JobInstance> jobInstances = scheduleJob(plan, planInstanceId, jobId);
        scheduleJobs(jobInstances);
    }

    /**
     * 调度planInstance下对应job
     */
    public List<JobInstance> scheduleJob(Plan plan, String planInstanceId, String jobId) {
        DAG<WorkflowJobInfo> dag = plan.getDag();
        WorkflowJobInfo jobInfo = dag.getNode(jobId);

        Verifies.verify(checkJobsSuccess(planInstanceId, dag.preNodes(jobInfo.getId()), true), "previous job is not complete, please wait!");

        Verifies.verify(TriggerType.API == jobInfo.getTriggerType(), "only api triggerType job can schedule by api");

        PlanInstance planInstance = planInstanceRepository.get(planInstanceId);

        List<JobInstance> jobInstances = createScheduleJobInstances(plan, planInstance.getAttributes(), planInstanceId, TimeUtils.currentLocalDateTime());

        jobInstanceRepository.saveAll(jobInstances);
        return jobInstances;
    }

    /**
     * 手工下发 job
     */
    public void manualScheduleJob(String planInstanceId, String jobId) {
        PlanInstance planInstance = planInstanceRepository.get(planInstanceId);
        Verifies.notNull(planInstance, MsgConstants.CANT_FIND_PLAN_INSTANCE + planInstanceId);
        Plan plan = planRepository.getByVersion(planInstance.getPlanId(), planInstance.getVersion());
        List<JobInstance> jobInstances = manualScheduleJob(plan, planInstanceId, jobId);
        scheduleJobs(jobInstances);
    }

    // todo 执行的时候可以选择 是就只重新计算当前的节点还是后续节点是否也重新执行一遍
    public List<JobInstance> manualScheduleJob(Plan plan, String planInstanceId, String jobId) {
        DAG<WorkflowJobInfo> dag = plan.getDag();
        WorkflowJobInfo jobInfo = dag.getNode(jobId);

        Verifies.verify(checkJobsSuccess(planInstanceId, dag.preNodes(jobInfo.getId()), true), "previous job is not complete, please wait!");

        JobInstance jobInstance = jobInstanceRepository.getLatest(planInstanceId, jobId);// 获取最后一条
        String newJobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
        jobInstance.retryReset(newJobInstanceId, 0);
        List<JobInstance> jobInstances = Collections.singletonList(jobInstance);

        jobInstanceRepository.saveAll(jobInstances);
        return jobInstances;
    }



    /**
     * 下发job给agent
     */
    public void schedule(JobInstance jobInstance) {
        if (jobInstance.getStatus() != JobStatus.SCHEDULING) {
            return;
        }

        // 选择 agent
        List<ScheduleAgent> agents = agentRegistry.all().stream()
                .filter(a -> a.getAvailableQueueLimit() > 0)
                .filter(ScheduleAgent::isEnabled)
                .collect(Collectors.toList());
        RPCInvocation lbInvocation = RPCInvocation.builder()
                .path(HttpAgentApi.API_JOB_RECEIVE)
                .build();
        ScheduleAgent agent = lbStrategy.select(agents, lbInvocation).orElse(null);
        if (agent == null) {
            // 状态检测的时候自动重试
            if (log.isDebugEnabled()) {
                log.debug("No alive server for job={}", jobInstance.getJobInstanceId());
            }
            return;
        }

        // rpc 执行
        try {
            log.info("Try dispatch JobInstance id={} to agent={}", jobInstance.getJobInstanceId(), agent.getId());
            boolean dispatched = agent.dispatch(jobInstance); // 可能存在接口超时导致重复下发，HttpBrokerApi.API_JOB_EXECUTING 由对应接口处理
            log.info("Dispatch JobInstance id={} to agent={} success={}", jobInstance.getJobInstanceId(), agent.getId(), dispatched);
        } catch (Exception e) {
            log.error("Dispatch JobInstance id={} to agent={} fail", jobInstance.getJobInstanceId(), agent.getId(), e);
        }
    }

    /**
     * job开始执行的反馈
     */
    public boolean jobExecuting(String agentId, String jobInstanceId) {
        log.info("Receive Job executing info agentId={} jobInstanceId={}", agentId, jobInstanceId);
        JobInstance jobInstance = jobInstanceRepository.get(jobInstanceId);
        planInstanceRepository.executing(jobInstance.getPlanInstanceId(), TimeUtils.currentLocalDateTime());
        return jobInstanceRepository.executing(jobInstanceId, agentId, TimeUtils.currentLocalDateTime());
    }

    /**
     * job执行上报
     */
    public boolean jobReport(String jobInstanceId) {
        log.info("Receive Job report jobInstanceId={}", jobInstanceId);
        return jobInstanceRepository.report(jobInstanceId, TimeUtils.currentLocalDateTime());
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
        List<JobInstance> jobInstances;
        switch (result) {
            case SUCCEED:
//                    jobInstance.setContext(new Attributes(param.getContext()));
                jobInstances = handleJobSuccess(jobInstance);
                break;

            case FAILED:
                jobInstances = handleJobFail(jobInstance, param.getErrorMsg());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");

            default:
                throw new IllegalStateException("Unexpect execute result: " + param.getResult());
        }
        scheduleJobs(jobInstances);
    }

    /**
     * 处理某个job实例执行成功
     */
    public List<JobInstance> handleJobSuccess(JobInstance jobInstance) {
        if (!jobInstanceRepository.success(jobInstance.getJobInstanceId(), TimeUtils.currentLocalDateTime(), jobInstance.getContext().toString())) {
            return Collections.emptyList(); // 被其他更新
        }

        String planInstanceId = jobInstance.getPlanInstanceId();

        String planId = jobInstance.getPlanId();
        String version = jobInstance.getPlanVersion();
        JobInfo jobInfo = jobInstance.getJobInfo();
        String jobId = jobInfo.getId();

        Plan plan = planRepository.getByVersion(planId, version);
        DAG<WorkflowJobInfo> dag = plan.getDag();

        // 当前节点的子节点
        List<WorkflowJobInfo> subJobInfos = dag.subNodes(jobId);

        // 防止并发问题，两个任务结束后并发过来后，由于无法读取到未提交数据，可能导致都认为不需要下发而导致失败
        PlanInstance planInstance = planInstanceEntityRepo.selectForUpdate(planInstanceId); // todo lock

        if (CollectionUtils.isEmpty(subJobInfos)) {
            // 当前节点为叶子节点 检测 Plan 实例是否已经执行完成
            // 1. 所有节点都已经成功或者失败 2. 这里只关心plan的成功更新，失败是在task回调
            if (checkJobsSuccess(planInstanceId, dag.lasts(), true)) {
                handlerPlanComplete(planInstanceId, true); // todo 这里需要调度
            }
            return Collections.emptyList();
        } else {
            LocalDateTime triggerAt = TimeUtils.currentLocalDateTime();
            // 后续作业存在，则检测是否可触发，并继续下发作业
            List<JobInstance> subJobInstances = new ArrayList<>();
            for (WorkflowJobInfo subJobInfo : subJobInfos) {
                // 前置节点已经完成则可以下发
                if (checkJobsSuccess(planInstanceId, dag.preNodes(subJobInfo.getId()), true)) {
                    JobInstance subJobInstance = createJobInstance(planId, version, planInstance.getType(), planInstanceId, planInstance.getAttributes(), jobInstance.getContext(), subJobInfo, triggerAt);
                    subJobInstances.add(subJobInstance);
                }
            }

            if (CollectionUtils.isNotEmpty(subJobInstances)) {
                jobInstanceRepository.saveAll(subJobInstances);
            }
            return subJobInstances;
        }

    }

    /**
     * 处理job实例执行失败
     */
    public List<JobInstance> handleJobFail(JobInstance jobInstance, String errorMsg) {
        LocalDateTime current = TimeUtils.currentLocalDateTime();
        LocalDateTime startAt = jobInstance.getStartAt() == null ? current : jobInstance.getStartAt();
        if (!jobInstanceRepository.fail(jobInstance.getJobInstanceId(), jobInstance.getStatus().status, startAt, current, errorMsg)) {
            return Collections.emptyList(); // 可能被其他的task处理了
        }

        WorkflowJobInfo jobInfo = (WorkflowJobInfo) jobInstance.getJobInfo();
        String planInstanceId = jobInstance.getPlanInstanceId();
        // 是否需要重试
        if (jobInstance.canRetry()) {
            String newJobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
            jobInstance.retryReset(newJobInstanceId, jobInfo.getRetryOption().getRetryInterval());
            jobInstanceRepository.saveAll(Collections.singletonList(jobInstance));
            return Collections.singletonList(jobInstance);
        } else if (jobInfo.isSkipWhenFail()) {
            // 如果 配置job失败了也继续执行
            return handleJobSuccess(jobInstance);
        } else {
            handlerPlanComplete(planInstanceId, false);
            return Collections.emptyList();
        }
    }


    private void scheduleJobs(List<JobInstance> scheduleJobs) {
        if (CollectionUtils.isEmpty(scheduleJobs)) {
            return;
        }
        for (JobInstance jobInstance : scheduleJobs) {
            CommonThreadPool.IO.submit(() -> schedule(jobInstance));
        }
    }

    public void handlerPlanComplete(String planInstanceId, boolean success) {
        PlanInstance planInstance = planInstanceRepository.get(planInstanceId);
        Verifies.notNull(planInstance, MsgConstants.CANT_FIND_PLAN_INSTANCE + planInstanceId);
        if (success) {
            planInstanceRepository.success(planInstanceId, TimeUtils.currentLocalDateTime());
        } else {
            LocalDateTime current = TimeUtils.currentLocalDateTime();
            LocalDateTime startAt = planInstance.getStartAt() == null ? current : planInstance.getStartAt();
            planInstanceRepository.fail(planInstanceId, startAt, current);
        }
        // 下发 fixed_delay 任务
        if (ScheduleType.FIXED_DELAY == planInstance.getScheduleOption().getScheduleType()) {
            PlanScheduleTask metaTask = metaTaskConverter.toPlanScheduleTask(planInstance.getPlanId(), TriggerType.SCHEDULE);
            metaTaskScheduler.unschedule(metaTask.scheduleId());
            metaTaskScheduler.schedule(metaTask);
        }
    }

    /**
     * 校验 planInstance 下对应 job 的 jobInstance 是否都执行成功 或者失败了但是可以忽略失败
     * @param checkSkipWhenFail 和 continueWithFail 同时 true，当job执行失败，会认为执行成功
     */
    public boolean checkJobsSuccess(String planInstanceId, List<WorkflowJobInfo> jobInfos, boolean checkSkipWhenFail) {
        if (CollectionUtils.isEmpty(jobInfos)) {
            return true;
        }

        for (WorkflowJobInfo jobInfo : jobInfos) {
            JobInstance jobInstance = jobInstanceRepository.getLatest(planInstanceId, jobInfo.getId());
            if (jobInstance == null) {
                // 按新流程 job 应该统一创建 不存在有些job还未创建情况的
                log.warn("job doesn't create completable in PlanInstance:{} where jobId:{}", planInstanceId, jobInfo.getId());
                return false;
            }
            if (jobInstance.getStatus() == JobStatus.FAILED) {
                if (!checkSkipWhenFail || !jobInfo.isSkipWhenFail()) {
                    return false;
                }

            } else if (jobInstance.getStatus() != JobStatus.SUCCEED) {
                return false; // 执行中
            }
        }
        return true;
    }

    /**
     * 创建调度触发的job
     */
    private List<JobInstance> createScheduleJobInstances(Plan plan, Attributes planAttributes, String planInstanceId, LocalDateTime triggerAt) {
        List<JobInstance> jobInstances = new ArrayList<>();
        // 获取头部节点
        for (WorkflowJobInfo jobInfo :  plan.getDag().origins()) {
            if (TriggerType.SCHEDULE == jobInfo.getTriggerType()) {
                jobInstances.add(createJobInstance(plan.getPlanId(), plan.getVersion(), plan.getType(), planInstanceId, planAttributes, new Attributes(), jobInfo, triggerAt));
            }
        }
        return jobInstances;
    }

    private PlanInstance createPlanInstance(Plan plan, Attributes planAttributes, LocalDateTime triggerAt) {
        String id = idGenerator.generateId(IDType.PLAN_INSTANCE);
        PlanInstance planInstance = new PlanInstance();
        planInstance.setId(id);
        planInstance.setPlanId(plan.getPlanId());
        planInstance.setVersion(plan.getVersion());
        planInstance.setStatus(ConstantsPool.PLAN_DISPATCHING);
        planInstance.setType(plan.getType());
        planInstance.setTriggerType(plan.getTriggerType());
        planInstance.setScheduleOption(plan.getScheduleOption());
        planInstance.setDag(plan.getDag());
        planInstance.setAttributes(planAttributes == null ? new Attributes() : planAttributes);
        planInstance.setTriggerAt(triggerAt);
        return planInstance;
    }

    private JobInstance createJobInstance(String planId, String planVersion, PlanType planType, String planInstanceId, Attributes planAttributes,
                                         Attributes context, JobInfo jobInfo, LocalDateTime triggerAt) {
        String jobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
        JobInstance instance = new JobInstance();
        instance.setJobInstanceId(jobInstanceId);
        instance.setAgentId("");
        instance.setJobInfo(jobInfo);
        instance.setPlanType(planType);
        instance.setPlanId(planId);
        instance.setPlanInstanceId(planInstanceId);
        instance.setPlanVersion(planVersion);
        instance.setStatus(JobStatus.SCHEDULING);
        instance.setTriggerAt(triggerAt);
        instance.setContext(context == null ? new Attributes() : context);
        Attributes attributes = new Attributes();
        attributes.put(planAttributes);
        attributes.put(jobInfo.getAttributes());
        instance.setAttributes(attributes);
        return instance;
    }

}
