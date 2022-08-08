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
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskStatus;
import org.limbo.flowjob.broker.application.plan.support.EventListener;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventTopic;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.plan.ScheduleEventTopic;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;
import org.limbo.flowjob.broker.core.repository.JobInstanceRepository;
import org.limbo.flowjob.broker.core.repository.PlanInstanceRepository;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Devil
 * @since 2022/8/5
 */
@Component
public class JobSuccessListener implements EventListener {

    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceRepository planInstanceRepository;

    @Override
    public EventTopic topic() {
        return ScheduleEventTopic.JOB_SUCCESS;
    }

    @Override
    @Transactional
    public void accept(Event event) {
        JobInstance jobInstance = (JobInstance) event.getSource();

        int num = jobInstanceEntityRepo.updateStatus(
                Long.valueOf(jobInstance.getJobInstanceId()),
                JobStatus.EXECUTING.status,
                JobStatus.SUCCEED.status
        );

        if (num != 1) {
            return;
        }

        // todo 这里获取到的 里面应该包含所有JobInstance
        PlanInstance planInstance = planInstanceRepository.get(jobInstance.getPlanInstanceId());
        planInstance.dispatchNext(jobInstance.getJobId());

    }

}
