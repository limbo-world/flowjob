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
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.api.param.broker.JobFeedbackParam;
import org.limbo.flowjob.broker.core.agent.AgentRegistry;
import org.limbo.flowjob.broker.core.cluster.Node;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.meta.IDGenerator;
import org.limbo.flowjob.broker.core.meta.IDType;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.meta.instance.Instance;
import org.limbo.flowjob.broker.core.meta.job.JobInstance;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceFactory;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.service.TransactionService;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Devil
 * @since 2024/1/4
 */
@Slf4j
public abstract class InstanceProcessor {

    protected final AgentRegistry agentRegistry;

    protected final NodeManger nodeManger;

    protected final IDGenerator idGenerator;

    protected final MetaTaskScheduler metaTaskScheduler;

    protected final TransactionService transactionService;

    protected final JobInstanceRepository jobInstanceRepository;

    protected InstanceProcessor(AgentRegistry agentRegistry,
                                NodeManger nodeManger,
                                IDGenerator idGenerator,
                                MetaTaskScheduler metaTaskScheduler,
                                TransactionService transactionService,
                                JobInstanceRepository jobInstanceRepository) {
        this.agentRegistry = agentRegistry;
        this.nodeManger = nodeManger;
        this.idGenerator = idGenerator;
        this.metaTaskScheduler = metaTaskScheduler;
        this.transactionService = transactionService;
        this.jobInstanceRepository = jobInstanceRepository;
    }

    /**
     * job开始执行的反馈
     */
    public boolean jobExecuting(String agentId, String jobInstanceId) {
        log.info("Receive Job executing info agentId={} jobInstanceId={}", agentId, jobInstanceId);
        JobInstance jobInstance = jobInstanceRepository.get(jobInstanceId);
        return transactionService.transactional(() -> {
            instanceExecuting(jobInstance.getInstanceId());
            return jobInstanceRepository.executing(jobInstanceId, agentId, TimeUtils.currentLocalDateTime());
        });
    }

    /**
     * 实例更新为执行中
     *
     * @param instanceId 实例ID
     * @return 是否成功
     */
    protected abstract boolean instanceExecuting(String instanceId);

    /**
     * job执行上报
     */
    public boolean jobReport(String jobInstanceId) {
        log.info("Receive Job report jobInstanceId={}", jobInstanceId);
        return transactionService.transactional(() -> jobInstanceRepository.report(jobInstanceId, TimeUtils.currentLocalDateTime()));
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
     * 处理某个job实例执行成功
     */
    protected ScheduleContext handleJobSuccess(JobInstance jobInstance) {
        String instanceId = jobInstance.getInstanceId();
        String planId = jobInstance.getPlanId();
        String version = jobInstance.getPlanVersion();
        WorkflowJobInfo jobInfo = jobInstance.getJobInfo();
        String jobId = jobInfo.getId();

        // 防止并发问题，两个任务结束后并发过来后，由于无法读取到未提交数据，可能导致都认为不需要下发而导致失败
        // 考虑到可能是RR级别，如果放到 jobInstanceRepository.success 之后 checkJobsSuccess 获取的数据可能是非完成状态导致一直无法完成
        Instance instance = lockAndGet(instanceId);

        ScheduleContext scheduleContext = new ScheduleContext();
        if (!jobInstanceRepository.success(jobInstance.getId(), TimeUtils.currentLocalDateTime(), jobInstance.getContext().toString())) {
            return scheduleContext; // 被其他更新
        }

        DAG<WorkflowJobInfo> dag = instance.getDag();

        // 当前节点的子节点
        List<WorkflowJobInfo> subJobInfos = dag.subNodes(jobId);

        if (CollectionUtils.isEmpty(subJobInfos)) {
            // 当前节点为叶子节点 检测 Plan 实例是否已经执行完成
            // 1. 所有节点都已经成功或者失败 2. 这里只关心plan的成功更新，失败是在task回调
            if (checkJobsSuccess(instanceId, dag.lasts(), true)) {
                handlerInstanceComplete(instanceId, true, scheduleContext);
            }
            return scheduleContext;
        } else {
            LocalDateTime triggerAt = TimeUtils.currentLocalDateTime();
            // 后续作业存在，则检测是否可触发，并继续下发作业
            List<JobInstance> subJobInstances = new ArrayList<>();
            for (WorkflowJobInfo subJobInfo : subJobInfos) {
                // 前置节点已经完成则可以下发
                if (checkJobsSuccess(instanceId, dag.preNodes(subJobInfo.getId()), true) && TriggerType.SCHEDULE == subJobInfo.getTriggerType()) {
                    String jobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
                    Node elect = nodeManger.elect(jobInstanceId);
                    JobInstance subJobInstance = JobInstanceFactory.create(jobInstanceId, planId, version, instanceId, elect.getUrl(), instance.getAttributes(), jobInstance.getContext(), subJobInfo, triggerAt);
                    subJobInstances.add(subJobInstance);
                }
            }

            if (CollectionUtils.isNotEmpty(subJobInstances)) {
                jobInstanceRepository.saveAll(subJobInstances);
                scheduleContext.setWaitScheduleJobs(subJobInstances);
            }
            return scheduleContext;
        }
    }

    /**
     * 处理job实例执行失败
     */
    protected ScheduleContext handleJobFail(JobInstance jobInstance, String errorMsg) {
        LocalDateTime current = TimeUtils.currentLocalDateTime();
        LocalDateTime startAt = jobInstance.getStartAt() == null ? current : jobInstance.getStartAt();
        ScheduleContext scheduleContext = new ScheduleContext();
        if (!jobInstanceRepository.fail(jobInstance.getId(), jobInstance.getStatus().status, startAt, current, errorMsg)) {
            return scheduleContext; // 可能被其他的task处理了
        }

        WorkflowJobInfo jobInfo = jobInstance.getJobInfo();
        String planInstanceId = jobInstance.getInstanceId();
        // 是否需要重试
        if (jobInstance.canRetry()) {
            String newJobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
            jobInstance.retryReset(newJobInstanceId, jobInfo.getRetryOption().getRetryInterval());
            List<JobInstance> jobInstances = Collections.singletonList(jobInstance);
            jobInstanceRepository.saveAll(jobInstances);
            scheduleContext.setWaitScheduleJobs(jobInstances);
            return scheduleContext;
        } else if (jobInfo.isSkipWhenFail()) {
            // 如果 配置job失败了也继续执行
            return handleJobSuccess(jobInstance);
        } else {
            handlerInstanceComplete(planInstanceId, false, scheduleContext);
            return scheduleContext;
        }
    }


    /**
     * 实例完成处理
     *
     * @param instanceId      实例ID
     * @param success         成功还是失败
     * @param scheduleContext 上下文数据
     */
    protected abstract void handlerInstanceComplete(String instanceId, boolean success, ScheduleContext scheduleContext);

    /**
     * 锁定实例资源
     *
     * @param instanceId 实例ID
     * @return 实例
     */
    protected abstract Instance lockAndGet(String instanceId);

    /**
     * 异步处理数据
     *
     * @param scheduleContext 产生的数据
     */
    protected abstract void asyncSchedule(ScheduleContext scheduleContext);

    /**
     * 校验 instance 下对应 job 的 jobInstance 是否都执行成功 或者失败了但是可以忽略失败
     * todo 性能
     *
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
}
