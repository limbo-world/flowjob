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

import lombok.Setter;
import org.limbo.flowjob.broker.api.constants.enums.ScheduleType;
import org.limbo.flowjob.broker.api.constants.enums.TriggerType;
import org.limbo.flowjob.broker.application.plan.support.EventListener;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventTopic;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.plan.ScheduleEventTopic;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanInstanceEntityRepo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Devil
 * @since 2022/8/5
 */
@Component
public class PlanExecutingListener implements EventListener {

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;
    @Setter(onMethod_ = @Inject)
    private PlanInstanceEntityRepo planInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private JobInstanceRepository jobInstanceRepository;
    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;

    @Override
    public EventTopic topic() {
        return ScheduleEventTopic.PLAN_EXECUTING;
    }

    @Override
    @Transactional
    public void accept(Event event) {
        PlanInstance planInstance = (PlanInstance) event.getSource();

        Long planId = Long.valueOf(planInstance.getPlanId());

        // 加锁
        planEntityRepo.selectForUpdate(planId);

        // 判断并发情况下 是否已经有人提交调度任务 如有则无需处理
        PlanInstanceEntity planInstanceEntity = planInstanceEntityRepo.findByPlanIdAndExpectTriggerAtAndTriggerType(planId, planInstance.getExpectTriggerAt(), TriggerType.SCHEDULE.type);
        if (planInstanceEntity != null) {
            return;
        }

        planInstanceRepository.save(planInstance);

        // 批量保存数据
        List<JobInstance> jobInstances = planInstance.getUnScheduledJobInstances();
        for (JobInstance jobInstance : jobInstances) {
            jobInstanceRepository.save(jobInstance);
        }

        // 更新plan的下次触发时间
        if (ScheduleType.FIXED_DELAY != planInstance.getScheduleOption().getScheduleType() && TriggerType.SCHEDULE == planInstance.getScheduleOption().getTriggerType()) {
            planEntityRepo.nextTriggerAt(planId, planInstance.nextTriggerAt());
        }
    }

}
