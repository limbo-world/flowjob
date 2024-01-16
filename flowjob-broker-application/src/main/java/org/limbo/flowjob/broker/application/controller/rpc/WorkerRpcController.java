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

package org.limbo.flowjob.broker.application.controller.rpc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.dto.broker.WorkerRegisterDTO;
import org.limbo.flowjob.api.param.broker.WorkerHeartbeatParam;
import org.limbo.flowjob.api.param.broker.WorkerRegisterParam;
import org.limbo.flowjob.broker.application.service.WorkerAppService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.limbo.flowjob.api.constants.rpc.HttpBrokerApi.API_WORKER_HEARTBEAT;
import static org.limbo.flowjob.api.constants.rpc.HttpBrokerApi.API_WORKER_REGISTER;

/**
 * @author Brozen
 * @since 2021-06-10
 */
@Tag(name = "worker remote rpc")
@RestController
public class WorkerRpcController {

    @Setter(onMethod_ = @Inject)
    private WorkerAppService workerAppService;

    /**
     * worker注册
     */
    @Operation(summary = "worker注册")
    @PostMapping(API_WORKER_REGISTER)
    public ResponseDTO<WorkerRegisterDTO> register(@Validated @RequestBody WorkerRegisterParam param) {
        return ResponseDTO.<WorkerRegisterDTO>builder().ok(workerAppService.register(param)).build();
    }

    /**
     * worker心跳
     */
    @Operation(summary = "worker心跳")
    @PostMapping(API_WORKER_HEARTBEAT)
    public ResponseDTO<WorkerRegisterDTO> heartbeat(@Validated @NotNull(message = "no id") @RequestParam("id") String id,
                                                    @Valid @RequestBody WorkerHeartbeatParam heartbeatOption) {
        return ResponseDTO.<WorkerRegisterDTO>builder().ok(workerAppService.heartbeat(id, heartbeatOption)).build();
    }

}
