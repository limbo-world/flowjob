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

package org.limbo.flowjob.agent.core.service;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.agent.core.entity.Task;
import org.limbo.flowjob.agent.core.repository.TaskRepository;
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.console.TaskDTO;
import org.limbo.flowjob.api.param.console.TaskQueryParam;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/8/4
 */
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public PageDTO<TaskDTO> page(TaskQueryParam param) {
        long count = taskRepository.queryCount(param);
        if (count <= 0) {
            return PageDTO.empty(param);
        }

        PageDTO<TaskDTO> page = PageDTO.convertByPage(param);
        page.setTotal(count);

        List<Task> tasks = taskRepository.queryPage(param);
        page.setData(tasks.stream().map(task -> {
            TaskDTO dto = new TaskDTO();
            dto.setTaskId(task.getId());
            dto.setJobInstanceId(task.getJobId());
            dto.setWorkerId(task.getWorker() == null ? "" : task.getWorker().getId());
            dto.setWorkerAddress(task.getWorker() == null ? "" : task.getWorker().address());
            dto.setType(task.getType().type);
            dto.setStatus(task.getStatus().status);
            dto.setContext(task.getContext().toString());
            dto.setJobAttributes(task.getJobAttributes().toString());
            dto.setTaskAttributes(task.getTaskAttributes());
            dto.setResult(task.getResult());
            dto.setErrorMsg(task.getErrorMsg());
            dto.setErrorStackTrace(task.getErrorStackTrace());
            dto.setStartAt(TimeUtils.toTimestamp(task.getStartAt()));
            dto.setEndAt(TimeUtils.toTimestamp(task.getEndAt()));
            return dto;
        }).collect(Collectors.toList()));
        return page;
    }

}
