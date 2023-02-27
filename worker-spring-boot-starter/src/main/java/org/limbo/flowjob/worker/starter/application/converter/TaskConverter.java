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

package org.limbo.flowjob.worker.starter.application.converter;

import org.limbo.flowjob.api.remote.param.TaskSubmitParam;
import org.limbo.flowjob.common.constants.TaskType;
import org.limbo.flowjob.worker.core.domain.Task;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-09-20
 */
@Component
public class TaskConverter {

    /**
     * Task 提交参数转为 Task
     */
    public Task task(TaskSubmitParam param) {
        TaskType taskType = TaskType.parse(param.getType());
        Task task = new Task();
        task.setTaskId(param.getTaskId());
        task.setPlanId(param.getPlanId());
        task.setPlanInstanceId(param.getPlanId());
        task.setJobId(param.getJobId());
        task.setJobInstanceId(param.getJobInstanceId());
        task.setType(taskType);
        task.setExecutorName(param.getExecutorName());
        task.setContext(param.getContext());
        task.setJobAttributes(param.getAttributes());
        switch (taskType) {
            case MAP:
                task.setMapAttributes(param.getMapAttributes());
                break;
            case REDUCE:
                List<Map<String, Object>> attr = param.getReduceAttributes();
                attr = attr.stream()
                        .map(Collections::unmodifiableMap)
                        .collect(Collectors.toList());
                task.setReduceAttributes(attr);
                break;
            default:
                break;
        }
        return task;
    }

}
