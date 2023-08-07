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

package org.limbo.flowjob.broker.application.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.dto.console.JobInstanceDTO;
import org.limbo.flowjob.api.dto.console.TaskDTO;
import org.limbo.flowjob.api.param.console.JobInstanceQueryParam;
import org.limbo.flowjob.api.param.console.TaskQueryParam;
import org.limbo.flowjob.broker.application.service.JobInstanceService;
import org.limbo.flowjob.broker.application.service.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @author Devil
 * @since 2023/5/11
 */
@Tag(name = "task console api")
@RestController
public class TaskController {

    @Setter(onMethod_ = @Inject)
    private TaskService taskService;

    /**
     * 计划列表
     */
    @Operation(summary = "任务列表")
    @GetMapping("/api/v1/task/page")
    public ResponseDTO<PageDTO<TaskDTO>> page(TaskQueryParam param) {
        return ResponseDTO.<PageDTO<TaskDTO>>builder().ok(taskService.page(param)).build();
    }

}
