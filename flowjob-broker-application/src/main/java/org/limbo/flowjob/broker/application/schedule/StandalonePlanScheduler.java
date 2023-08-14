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

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.constants.PlanType;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.core.domain.IDType;
import org.limbo.flowjob.broker.core.domain.plan.NormalPlan;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.utils.Verifies;
import org.limbo.flowjob.broker.core.domain.job.JobInfo;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author Devil
 * @since 2023/8/7
 */
@Slf4j
@Component
public class StandalonePlanScheduler extends AbstractPlanScheduler {


    @Override
    public List<JobInstance> createJobInstances(Plan plan, String planInstanceId, LocalDateTime triggerAt) {
        JobInfo jobInfo = ((NormalPlan) plan).getJobInfo();
        JobInstance jobInstance = createJobInstance(plan.getPlanId(), plan.getVersion(), planInstanceId, new Attributes(), jobInfo, triggerAt);
        return Collections.singletonList(jobInstance);
    }

    @Override
    @Transactional
    public void handleJobSuccess(JobInstance jobInstance) {
        int num = jobInstanceEntityRepo.success(jobInstance.getJobInstanceId(), TimeUtils.currentLocalDateTime(), jobInstance.getContext().toString());
        if (num < 1) {
            return; // 被其他更新
        }

        // 更新 plan 上下文
//        planInstanceEntityRepo.updateContext(jobInstance.getPlanInstanceId(), jobInstance.getContext().toString());

        String planInstanceId = jobInstance.getPlanInstanceId();
        handlerPlanComplete(planInstanceId, true);
    }

    @Override
    @Transactional
    public void handleJobFail(JobInstance jobInstance, String errorMsg) {
        int num = jobInstanceEntityRepo.fail(jobInstance.getJobInstanceId(), errorMsg);
        if (num < 1) {
            return; // 可能被其他的处理了
        }

        JobInfo jobInfo = jobInstance.getJobInfo();
        String planInstanceId = jobInstance.getPlanInstanceId();
        // 是否需要重试
        if (jobInstance.canRetry()) {
            String newJobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
            jobInstance.retryReset(newJobInstanceId, jobInfo.getRetryOption().getRetryInterval());
            saveAndScheduleJobInstances(Collections.singletonList(jobInstance));
        } else {
            handlerPlanComplete(planInstanceId, false);
        }
    }

    @Override
    @Transactional
    public void scheduleJob(Plan plan, String planInstanceId, String jobId) {
        Verifies.verify(TriggerType.API == plan.getTriggerType(), "only api triggerType job can schedule by api");

        List<JobInstance> jobInstances = createJobInstances(plan, planInstanceId, TimeUtils.currentLocalDateTime());

        saveAndScheduleJobInstances(jobInstances);
    }

    @Override
    @Transactional
    public void manualScheduleJob(Plan plan, String planInstanceId, String jobId) {
        JobInstance jobInstance = jobInstanceRepository.getLatest(planInstanceId, jobId);// 获取最后一条
        String newJobInstanceId = idGenerator.generateId(IDType.JOB_INSTANCE);
        jobInstance.retryReset(newJobInstanceId, 0);
        List<JobInstance> jobInstances = Collections.singletonList(jobInstance);
        saveAndScheduleJobInstances(jobInstances);
    }

    @Override
    public PlanType getPlanType() {
        return PlanType.STANDALONE;
    }
}
