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

package org.limbo.flowjob.broker.application.plan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.limbo.flowjob.api.remote.dto.ResponseDTO;
import org.limbo.flowjob.api.remote.dto.WorkerRegisterDTO;
import org.limbo.flowjob.api.remote.param.TaskFeedbackParam;
import org.limbo.flowjob.api.remote.param.WorkerHeartbeatParam;
import org.limbo.flowjob.api.remote.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.application.plan.service.TaskService;
import org.limbo.flowjob.broker.application.plan.service.WorkerService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author Brozen
 * @since 2021-06-10
 */
@Tag(name = "对worker提供的相关接口")
@RestController
@RequestMapping("/api/v1/rpc/worker")
public class WorkerRpcController {

    @Setter(onMethod_ = @Inject)
    private WorkerService workerService;

    @Setter(onMethod_ = @Inject)
    private TaskService taskService;

    /**
     * worker注册
     */
    @Operation(summary = "worker注册")
    @PostMapping
    public ResponseDTO<WorkerRegisterDTO> register(@Validated @RequestBody WorkerRegisterParam param) {
        return ResponseDTO.<WorkerRegisterDTO>builder().ok(workerService.register(param)).build();
    }


    /**
     * worker心跳
     */
    @Operation(summary = "worker心跳")
    @PostMapping("/{workerId}/heartbeat")
    public ResponseDTO<WorkerRegisterDTO> heartbeat(@Validated @NotNull(message = "no workerId") @PathVariable("workerId") String workerId,
                                                    @Valid @RequestBody WorkerHeartbeatParam heartbeatOption) {
        return ResponseDTO.<WorkerRegisterDTO>builder().ok(workerService.heartbeat(workerId, heartbeatOption)).build();
    }

    /**
     * 任务执行反馈接口
     */
    @Operation(summary = "任务执行反馈接口")
    @PostMapping("/task/{taskId}/feedback")
    public ResponseDTO<Void> feedback(@Validated @NotNull(message = "no taskId") @PathVariable("taskId") String taskId,
                                      @Valid @RequestBody TaskFeedbackParam feedback) {
        taskService.taskFeedback(taskId, feedback);
        return ResponseDTO.<Void>builder().ok().build();
    }

}
