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
import org.apache.commons.collections4.MapUtils;
import org.limbo.flowjob.api.constants.ExecuteResult;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.api.param.broker.JobFeedbackParam;
import org.limbo.flowjob.broker.application.task.JobScheduleTask;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanRepository;
import org.limbo.flowjob.broker.core.exceptions.VerifyException;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 代理 平衡事务
 *
 * @author Devil
 * @since 2023/2/8
 */
@Slf4j
@Component
public class ScheduleProxy implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Setter(onMethod_ = @Inject)
    private MetaTaskScheduler metaTaskScheduler;

    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepository;

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;


    private final Map<PlanType, PlanScheduler> schedulers = new EnumMap<>(PlanType.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void init() {
        Map<String, PlanScheduler> map = applicationContext.getBeansOfType(PlanScheduler.class);
        if (MapUtils.isEmpty(map)) {
            return;
        }
        for (Map.Entry<String, PlanScheduler> entry : map.entrySet()) {
            PlanScheduler scheduler = entry.getValue();
            schedulers.put(scheduler.getPlanType(), scheduler);
        }
    }

    /**
     * 调度 plan 创建PlanInstance并执行调度
     */
    public void schedule(TriggerType triggerType, Plan plan, Attributes planAttributes, LocalDateTime triggerAt) {
        executeWithAspect(unused -> schedulers.get(plan.getType()).schedule(triggerType, plan, planAttributes, triggerAt));
    }

    /**
     * 调度已有的JobInstance
     */
    public void schedule(JobInstance jobInstance) {
        executeWithAspect(unused -> schedulers.get(jobInstance.getPlanType()).schedule(jobInstance));
    }

    /**
     * job开始执行的反馈
     *
     * @param agentId
     * @param jobInstanceId
     * @return
     */
    @Transactional(rollbackOn = Throwable.class)
    public boolean jobExecuting(String agentId, String jobInstanceId) {
        log.info("Receive Job executing info agentId={} jobInstanceId={}", agentId, jobInstanceId);
        JobInstanceEntity jobInstanceEntity = jobInstanceEntityRepo.findById(jobInstanceId).orElse(null);
        planInstanceEntityRepo.executing(jobInstanceEntity.getPlanInstanceId(), TimeUtils.currentLocalDateTime());
        return jobInstanceEntityRepo.executing(jobInstanceId, agentId, TimeUtils.currentLocalDateTime()) > 0;
    }

    /**
     * job执行上报
     */
    @Transactional(rollbackOn = Throwable.class)
    public boolean jobReport(String jobInstanceId) {
        log.info("Receive Job report jobInstanceId={}", jobInstanceId);
        return jobInstanceEntityRepo.report(jobInstanceId, TimeUtils.currentLocalDateTime()) > 0;
    }

    /**
     * api 方式下发节点任务
     */
    public void scheduleJob(String planInstanceId, String jobId) {
        executeWithAspect(unused -> {
            PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(planInstanceId).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INSTANCE + planInstanceId));
            Plan plan = planRepository.getByVersion(planInstanceEntity.getPlanId(), planInstanceEntity.getPlanInfoId());
            schedulers.get(plan.getType()).scheduleJob(plan, planInstanceId, jobId);
        });
    }

    /**
     * 手工下发 job
     */
    public void manualScheduleJob(String planInstanceId, String jobId) {
        executeWithAspect(unused -> {
            PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findById(planInstanceId).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN_INSTANCE + planInstanceId));
            Plan plan = planRepository.getByVersion(planInstanceEntity.getPlanId(), planInstanceEntity.getPlanInfoId());
            schedulers.get(plan.getType()).manualScheduleJob(plan, planInstanceId, jobId);
        });
    }

    /**
     * 任务执行反馈
     *
     * @param jobInstanceId Id
     * @param param         反馈参数
     */
    public void feedback(String jobInstanceId, JobFeedbackParam param) {
        executeWithAspect(unused -> {
            ExecuteResult result = param.getResult();
            if (log.isDebugEnabled()) {
                log.debug("receive job feedback id:{} result:{}", jobInstanceId, result);
            }

            JobInstance jobInstance = jobInstanceRepository.get(jobInstanceId);
            Verifies.notNull(jobInstance, "job instance is null id:" + jobInstanceId);

            PlanScheduler planScheduler = schedulers.get(jobInstance.getPlanType());

            switch (result) {
                case SUCCEED:
//                    jobInstance.setContext(new Attributes(param.getContext()));
                    planScheduler.handleJobSuccess(jobInstance);
                    break;

                case FAILED:
                    planScheduler.handleJobFail(jobInstance, param.getErrorMsg());
                    break;

                case TERMINATED:
                    throw new UnsupportedOperationException("暂不支持手动终止任务");

                default:
                    throw new IllegalStateException("Unexpect execute result: " + param.getResult());
            }
        });
    }

    public void executeWithAspect(Consumer<Void> consumer) {
        try {
            // new context
            ScheduleContext.set();
            // do real
            consumer.accept(null);
            // do after
            scheduleJobs();
        } finally {
            // clear context
            ScheduleContext.clear();
        }
    }

    public void scheduleJobs() {
        if (CollectionUtils.isEmpty(ScheduleContext.waitScheduleJobs())) {
            return;
        }
        for (JobInstance jobInstance : ScheduleContext.waitScheduleJobs()) {
            try {
                metaTaskScheduler.schedule(new JobScheduleTask(jobInstance, this));
            } catch (Exception e) {
                // 由task的状态检查任务去修复task的执行情况
                log.error("task schedule fail! jobInstance={}", jobInstance, e);
            }
        }
    }

}
