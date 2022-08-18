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

package org.limbo.flowjob.broker.application.plan.service;

import lombok.Setter;
import org.limbo.flowjob.broker.api.constants.enums.JobStatus;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.repository.TaskRepository;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Devil
 * @since 2022/8/18
 */
@Service
public class JobService {

    @Setter(onMethod_ = @Inject)
    private JobInstanceEntityRepo jobInstanceEntityRepo;
    @Setter(onMethod_ = @Inject)
    private TaskRepository taskRepository;

    @Transactional
    public void saveDispatchInfo(JobInstance jobInstance, JobInstance.Tasks tasks) {
        if (jobInstanceEntityRepo.updateStatus(Long.valueOf(jobInstance.getJobId()), JobStatus.SCHEDULING.status, JobStatus.EXECUTING.status) != 1) {
            return;
        }

        for (Task task : tasks.getTasks()) {
            taskRepository.save(task);
        }

    }
}
