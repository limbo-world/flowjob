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
import org.limbo.flowjob.api.dto.WorkerRegisterDTO;
import org.limbo.flowjob.api.param.WorkerHeartbeatParam;
import org.limbo.flowjob.api.param.WorkerRegisterParam;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.broker.application.plan.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Brozen
 * @since 2021-06-10
 */
@Tag(name = "worker相关接口")
@RestController
@RequestMapping("/api/v1/worker")
public class WorkerController {

    @Autowired
    private WorkerService workerService;

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
    @PostMapping("/heartbeat")
    public ResponseDTO<WorkerRegisterDTO> heartbeat(@RequestBody WorkerHeartbeatParam heartbeatOption) {
        return ResponseDTO.<WorkerRegisterDTO>builder().ok(workerService.heartbeat(heartbeatOption)).build();
    }


}