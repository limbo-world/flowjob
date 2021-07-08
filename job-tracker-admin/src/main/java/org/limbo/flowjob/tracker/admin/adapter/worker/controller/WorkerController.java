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

package org.limbo.flowjob.tracker.admin.adapter.worker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.limbo.flowjob.tracker.admin.service.worker.WorkerRegisterService;
import org.limbo.flowjob.tracker.admin.service.worker.WorkerService;
import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerHeartbeatOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterResult;
import org.limbo.flowjob.tracker.commons.utils.Symbol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author Brozen
 * @since 2021-06-10
 */
@Tag(name = "worker相关接口")
@RestController
@RequestMapping("/api/worker/v1")
public class WorkerController {

    @Autowired
    private WorkerService workerService;

    @Autowired
    private WorkerRegisterService registerService;


    /**
     * worker注册
     */
    @Operation(summary = "worker注册")
    @PostMapping
    public Mono<ResponseDto<WorkerRegisterResult>> register(@Validated @RequestBody Mono<WorkerRegisterOptionDto> options) {
        return options.map(opt -> registerService.register(opt))
                .flatMap(result -> result.map(r -> ResponseDto.<WorkerRegisterResult>builder().ok(r).build()));
    }


    /**
     * worker心跳
     */
    @Operation(summary = "worker心跳")
    @PostMapping("/heartbeat")
    public Mono<ResponseDto<Void>> heartbeat(@RequestBody WorkerHeartbeatOptionDto heartbeatOption) {
        return workerService.heartbeat(heartbeatOption)
                .map(result -> ResponseDto.<Void>builder().ok().build());
    }


}
