/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.worker.starter.application.services;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.remote.param.TaskSubmitParam;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.starter.application.converter.TaskConverter;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
@Service("fjwWorkerService")
@AllArgsConstructor
public class WorkerService {

    private final Worker worker;

    @Setter(onMethod_ = @Inject)
    private TaskConverter taskConverter;

    /**
     * {@inheritDoc}
     * @param param
     * @return
     */
    public Boolean receive(TaskSubmitParam param) {
        log.info("receive task {}", param);
        try {
            Task task = taskConverter.task(param);
            worker.receiveTask(task);
            return true;
        } catch (Exception e) {
            log.error("Failed to receive task", e);
            return false;
        }
    }

}
