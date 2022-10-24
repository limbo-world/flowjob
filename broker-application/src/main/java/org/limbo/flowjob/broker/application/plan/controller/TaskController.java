/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.application.plan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.limbo.flowjob.broker.api.clent.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.broker.application.plan.service.TaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;

/**
 * @author Brozen
 * @since 2021-07-07
 */
@Tag(name = "任务执行相关接口")
@RestController
@RequestMapping("/api/v1/task")
public class TaskController {

    @Inject
    private TaskService taskService;


    /**
     * 任务执行反馈接口
     */
    @Operation(summary = "任务执行反馈接口")
    @PostMapping("/feedback")
    public ResponseDTO<Void> feedback(@Valid @RequestBody TaskFeedbackParam feedback) {
        taskService.feedback(feedback);
        return ResponseDTO.<Void>builder().ok().build();
    }


}
