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

package org.limbo.flowjob.broker.application.converter;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.broker.application.schedule.ScheduleStrategy;
import org.limbo.flowjob.broker.application.task.DelayJobInstanceScheduleTask;
import org.limbo.flowjob.broker.application.task.PlanInstanceScheduleTask;
import org.limbo.flowjob.broker.application.task.PlanScheduleTask;
import org.limbo.flowjob.broker.application.task.TaskScheduleTask;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanRepository;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author Devil
 * @since 2023/5/8
 */
@Slf4j
@Component
public class MetaTaskConverter {

    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private ScheduleStrategy scheduleStrategy;

    @Setter(onMethod_ = @Inject)
    private MetaTaskScheduler metaTaskScheduler;

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepository;

    public PlanScheduleTask toPlanScheduleTask(String planId, TriggerType triggerType) {
        Plan plan = planRepository.get(planId);
        // 获取最近一次调度的planInstance和最近一次结束的planInstance
        ScheduleOption scheduleOption = plan.getScheduleOption();
        PlanInstanceEntity latelyTrigger = planInstanceEntityRepo.findLatelyTrigger(planId, plan.getVersion(), scheduleOption.getScheduleType().type, triggerType.type);
        PlanInstanceEntity latelyFeedback = planInstanceEntityRepo.findLatelyFeedback(planId, plan.getVersion(), scheduleOption.getScheduleType().type, triggerType.type);

        LocalDateTime latelyTriggerAt = latelyTrigger == null || latelyTrigger.getTriggerAt() == null ? null : latelyTrigger.getTriggerAt().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime latelyFeedbackAt = latelyFeedback == null || latelyFeedback.getFeedbackAt() == null ? null : latelyFeedback.getFeedbackAt().truncatedTo(ChronoUnit.SECONDS);

        return new PlanScheduleTask(
                plan,
                latelyTriggerAt,
                latelyFeedbackAt,
                scheduleStrategy,
                metaTaskScheduler
        );

    }

    public TaskScheduleTask toTaskScheduleTask(Task task) {
        return new TaskScheduleTask(task, scheduleStrategy);
    }

    public TaskScheduleTask toTaskScheduleTask(TaskEntity entity) {
        Task task = DomainConverter.toTask(entity);
        return new TaskScheduleTask(task, scheduleStrategy);
    }

    public PlanInstanceScheduleTask toPlanInstanceScheduleTask(PlanInstanceEntity entity) {
        Plan plan = planRepository.getByVersion(entity.getPlanId(), entity.getPlanInfoId());
        return new PlanInstanceScheduleTask(entity.getPlanInstanceId(), plan, entity.getTriggerAt(), scheduleStrategy);
    }

    public DelayJobInstanceScheduleTask toJobInstanceScheduleTask(JobInstanceEntity entity) {
        JobInstance jobInstance = jobInstanceRepository.get(entity.getJobInstanceId());
        return new DelayJobInstanceScheduleTask(jobInstance, scheduleStrategy);
    }

}
