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

package org.limbo.flowjob.broker.application.component;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.core.agent.AgentRegistry;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanInstanceRepository;
import org.limbo.flowjob.broker.core.domain.plan.PlanRepository;
import org.limbo.flowjob.broker.core.schedule.SchedulerProcessor;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部分逻辑包装了事务
 *
 * @author Devil
 * @since 2023/12/7
 */
@Slf4j
@Component
public class FlowSchedulerProcessor extends SchedulerProcessor {

    private PlatformTransactionManager platformTransactionManager;

    public FlowSchedulerProcessor(MetaTaskScheduler metaTaskScheduler,
                                  IDGenerator idGenerator,
                                  AgentRegistry agentRegistry,
                                  PlanRepository planRepository,
                                  PlanInstanceRepository planInstanceRepository,
                                  JobInstanceRepository jobInstanceRepository,
                                  PlatformTransactionManager platformTransactionManager) {
        super(metaTaskScheduler, idGenerator, agentRegistry, planRepository, planInstanceRepository, jobInstanceRepository);
        this.platformTransactionManager = platformTransactionManager;
    }

    @Override
    public List<JobInstance> schedulePlan(String planId, TriggerType triggerType, Attributes planAttributes, LocalDateTime triggerAt) {
        TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            List<JobInstance> result = super.schedulePlan(planId, triggerType, planAttributes, triggerAt);
            platformTransactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(transaction);
            log.error("Transaction error and rollback", e);
            throw e;
        }
    }

    @Override
    public List<JobInstance> scheduleJob(Plan plan, String planInstanceId, String jobId) {
        TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            List<JobInstance> result = super.scheduleJob(plan, planInstanceId, jobId);
            platformTransactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(transaction);
            log.error("Transaction error and rollback", e);
            throw e;
        }
    }

    @Override
    public List<JobInstance> manualScheduleJob(Plan plan, String planInstanceId, String jobId) {
        TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            List<JobInstance> result = super.manualScheduleJob(plan, planInstanceId, jobId);
            platformTransactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(transaction);
            log.error("Transaction error and rollback", e);
            throw e;
        }
    }

    @Override
    public boolean jobExecuting(String agentId, String jobInstanceId) {
        TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            boolean result = super.jobExecuting(agentId, jobInstanceId);
            platformTransactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(transaction);
            log.error("Transaction error and rollback", e);
            throw e;
        }
    }

    @Override
    public boolean jobReport(String jobInstanceId) {
        TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            boolean result = super.jobReport(jobInstanceId);
            platformTransactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(transaction);
            log.error("Transaction error and rollback", e);
            throw e;
        }
    }

    @Override
    public List<JobInstance> handleJobSuccess(JobInstance jobInstance) {
        TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            List<JobInstance> result = super.handleJobSuccess(jobInstance);
            platformTransactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(transaction);
            log.error("Transaction error and rollback", e);
            throw e;
        }
    }

    @Override
    public List<JobInstance> handleJobFail(JobInstance jobInstance, String errorMsg) {
        TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            List<JobInstance> result = super.handleJobFail(jobInstance, errorMsg);
            platformTransactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(transaction);
            log.error("Transaction error and rollback", e);
            throw e;
        }
    }
}
