/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.start.application;

import org.limbo.flowjob.tracker.commons.dto.job.JobInstanceDto;
import org.limbo.flowjob.worker.core.domain.Job;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.springframework.stereotype.Service;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Service
public class WorkerService {

    private final Worker worker;

    public WorkerService(Worker worker) {
        this.worker = worker;
    }

    public void receive(JobInstanceDto dto) {
        Job job = new Job();
        job.setId(dto.getId());
        job.setExecutorName(dto.getExecutorName());
        job.setExecutorParam(dto.getExecutorParam());
        worker.receive(job);
    }

}
