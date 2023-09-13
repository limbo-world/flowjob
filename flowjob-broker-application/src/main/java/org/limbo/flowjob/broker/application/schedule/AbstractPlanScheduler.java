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
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.api.constants.rpc.HttpAgentApi;
import org.limbo.flowjob.broker.application.component.AgentRegistry;
import org.limbo.flowjob.broker.application.service.PlanInstanceService;
import org.limbo.flowjob.broker.core.agent.AgentRepository;
import org.limbo.flowjob.broker.core.agent.ScheduleAgent;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.AgentEntity;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.lb.LBStrategy;
import org.limbo.flowjob.common.lb.strategies.RoundRobinLBStrategy;
import org.limbo.flowjob.common.rpc.RPCInvocation;
import org.limbo.flowjob.common.utils.attribute.Attributes;
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
    protected JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    protected JobInstanceRepository jobInstanceRepository;

    @Setter(onMethod_ = @Inject)
    protected PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    protected PlanEntityRepo planEntityRepo;

    @Setter(onMethod_ = @Inject)
    protected IDGenerator idGenerator;

    @Setter(onMethod_ = @Inject)
    protected AgentRepository agentRepository;

    @Setter(onMethod_ = @Inject)
    protected MetaTaskScheduler metaTaskScheduler;

    @Setter(onMethod_ = @Inject)
    protected PlanInstanceService planInstanceService;

    @Setter(onMethod_ = @Inject)
    protected AgentRegistry agentRegistry;

    protected LBStrategy<ScheduleAgent> lbStrategy = new RoundRobinLBStrategy<>();

    @Transactional
    public void schedule(TriggerType triggerType, Plan plan, Attributes planAttributes, LocalDateTime triggerAt) {
        // 悲观锁快速释放，不阻塞后续任务
        String planInstanceId = idGenerator.generateId(IDType.PLAN_INSTANCE);
        planInstanceService.save(planInstanceId, planAttributes, triggerType, plan, triggerAt); // 可以考虑 PlanInstance 对象来处理后续流程
        List<JobInstance> jobInstances = createJobInstances(plan, planAttributes, planInstanceId, triggerAt);
        saveAndScheduleJobInstances(jobInstances);
        planInstanceEntityRepo.dispatching(planInstanceId);
    }

    // 如是定时1小时后执行，task的创建问题 比如任务执行失败后，重试间隔可能导致这个问题
    // 比如广播模式下，一小时后的节点数和当前的肯定是不同的
    protected void saveAndScheduleJobInstances(List<JobInstance> jobInstances) {
        saveJobInstances(jobInstances);
        ScheduleContext.waitScheduleJobs(jobInstances);
    }

    public abstract List<JobInstance> createJobInstances(Plan plan, Attributes planAttributes, String planInstanceId, LocalDateTime triggerAt);

    @Transactional
    public void saveJobInstances(List<JobInstance> jobInstances) {
        if (CollectionUtils.isEmpty(jobInstances)) {
            return;
        }

        // 如果是 plan instance 的检测，那么 job instance 已经创建，则无需再度创建

        // 保存 jobInstance
        List<JobInstanceEntity> jobInstanceEntities = jobInstances.stream().map(DomainConverter::toJobInstanceEntity).collect(Collectors.toList());
        jobInstanceEntityRepo.saveAll(jobInstanceEntities);
        jobInstanceEntityRepo.flush();
    }

    @Override
    @Transactional
    public void schedule(JobInstance jobInstance) {
        // 加锁 同时只能有一个在执行下发逻辑
        if (jobInstance.getStatus() != JobStatus.SCHEDULING) {
            return;
        }

        // 选择 agent
        List<ScheduleAgent> agents = agentRegistry.all().stream()
                .filter(a -> a.getAvailableQueueLimit() > 0)
                .filter(AgentEntity::isEnabled)
                .map(DomainConverter::toAgent)
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

    public void handlerPlanComplete(String planInstanceId, boolean success) {
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(planInstanceId).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INSTANCE + planInstanceId));
        if (success) {
            planInstanceEntityRepo.success(planInstanceId, TimeUtils.currentLocalDateTime());
        } else {
            LocalDateTime current = TimeUtils.currentLocalDateTime();
            LocalDateTime startAt = planInstanceEntity.getStartAt() == null ? current : planInstanceEntity.getStartAt();
            planInstanceEntityRepo.fail(planInstanceId, startAt, current);
        }
        if (ScheduleType.FIXED_DELAY == ScheduleType.parse(planInstanceEntity.getScheduleType())) {
            // 如果为 FIXED_DELAY 更新 plan  使得 UpdatedPlanLoadTask 进行重新加载
            planEntityRepo.updateTime(planInstanceEntity.getPlanId(), TimeUtils.currentLocalDateTime());
        }
    }

    public JobInstance createJobInstance(String planId, String planVersion, String planInstanceId, Attributes planAttributes,
                                         Attributes context, JobInfo jobInfo, LocalDateTime triggerAt) {
        String jobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
        JobInstance instance = new JobInstance();
        instance.setJobInstanceId(jobInstanceId);
        instance.setAgentId("");
        instance.setJobInfo(jobInfo);
        instance.setPlanType(getPlanType());
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
