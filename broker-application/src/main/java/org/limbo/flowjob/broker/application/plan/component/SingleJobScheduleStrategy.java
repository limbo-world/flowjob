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

package org.limbo.flowjob.broker.application.plan.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.plan.PlanInfo;
import org.limbo.flowjob.broker.core.domain.plan.SinglePlanInfo;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.exceptions.JobException;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.TaskType;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Devil
 * @since 2023/1/4
 */
@Slf4j
@Component
public class SingleJobScheduleStrategy extends AbstractScheduleStrategy {

    @Override
    protected void schedulePlanInfo(TriggerType triggerType, PlanInfo planInfo, LocalDateTime triggerAt) {
        String planId = planInfo.getPlanId();
        String version = planInfo.getVersion();

        SinglePlanInfo singlePlanInfo = (SinglePlanInfo) planInfo;
        // 保存 planInstance
        String planInstanceId = savePlanInstanceEntity(planId, version, triggerType, triggerAt);
        JobInstance jobInstance = newJobInstance(planId, version, planInfo.planType(), planInstanceId, singlePlanInfo.getJobInfo(), TimeUtils.currentLocalDateTime());
        scheduleJobInstance(jobInstance);
    }

    @Override
    public void handleJobSuccess(JobInstance jobInstance) {
        jobInstanceEntityRepo.updateStatusSuccess(jobInstance.getJobInstanceId());
        planInstanceEntityRepo.success(jobInstance.getPlanInstanceId(), TimeUtils.currentLocalDateTime());
    }

    @Override
    @Transactional
    public void handleJobFail(JobInstance jobInstance) {
        jobInstanceEntityRepo.updateStatusExecuteFail(jobInstance.getJobInstanceId(), MsgConstants.TASK_FAIL);
        if (jobInstance.retry()) {
            scheduleJobInstance(jobInstance);
        } else {
            planInstanceEntityRepo.fail(jobInstance.getPlanInstanceId(), TimeUtils.currentLocalDateTime());
        }
    }

    /**
     * 调度jobInstance
     */
    private void scheduleJobInstance(JobInstance jobInstance) {
        // 保存 jobInstance
        JobInstanceEntity jobInstanceEntity = domainConverter.toJobInstanceEntity(jobInstance);
        jobInstanceEntityRepo.saveAndFlush(jobInstanceEntity);

        // 根据job类型创建task
        List<Task> tasks;
        switch (jobInstance.getType()) {
            case NORMAL:
                tasks = taskFactory.create(jobInstance, jobInstance.getTriggerAt(), TaskType.NORMAL);
                break;
            case BROADCAST:
                tasks = taskFactory.create(jobInstance, jobInstance.getTriggerAt(), TaskType.BROADCAST);
                break;
            case MAP:
            case MAP_REDUCE:
                tasks = taskFactory.create(jobInstance, jobInstance.getTriggerAt(), TaskType.SPLIT);
                break;
            default:
                throw new JobException(jobInstance.getJobId(), MsgConstants.UNKNOWN + " job type:" + jobInstance.getType().type);
        }

        // 如果可以创建的任务为空（只有为广播任务）
        if (CollectionUtils.isEmpty(tasks)) {
            handleJobSuccess(jobInstance);
        } else {
            saveAndScheduleTask(tasks);
        }

    }

}
