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

package org.limbo.flowjob.worker.application.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.clent.dto.TaskReceiveDTO;
import org.limbo.flowjob.broker.api.clent.param.TaskSubmitParam;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.starter.processor.Executor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
@Service
@AllArgsConstructor
public class WorkerService {

    private final Worker worker;

    /**
     * {@inheritDoc}
     * @param param
     * @return
     */
    public TaskReceiveDTO receive(TaskSubmitParam param) {
        Task task = new Task();
        task.setTaskId(param.getTaskId());
        task.setPlanId(param.getPlanId());
        task.setPlanInstanceId(param.getPlanInstanceId());
        task.setJobId(param.getJobId());
        task.setJobInstanceId(param.getJobInstanceId());
        task.setType(param.getType());
        task.setExecutorName(param.getExecutorName());
        task.setContext(Collections.unmodifiableMap(param.getContext()));
        task.setAttributes(Collections.unmodifiableMap(param.getAttributes()));
        task.setMapAttributes(Collections.unmodifiableMap(param.getMapAttributes()));

        List<Map<String, Object>> attr = param.getReduceAttributes();
        attr = attr.stream()
                .map(Collections::unmodifiableMap)
                .collect(Collectors.toList());
        task.setReduceAttributes(Collections.unmodifiableList(attr));

        try {
            worker.receiveTask(task);
            return new TaskReceiveDTO(param.getTaskId(), true);
        } catch (Exception e) {
            log.error("Failed to receive task", e);
            return new TaskReceiveDTO(param.getTaskId(), false);
        }
    }


    @Executor
    public void hello(Task task) {
        log.info("Execute task {}", task.getId());
    }

}
