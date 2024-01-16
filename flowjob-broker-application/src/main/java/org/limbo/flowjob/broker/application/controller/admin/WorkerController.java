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
import org.limbo.flowjob.api.dto.console.WorkerDTO;
import org.limbo.flowjob.api.param.console.WorkerQueryParam;
import org.limbo.flowjob.broker.application.service.WorkerAppService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Tag(name = "worker console api")
@RestController
public class WorkerController {

    @Setter(onMethod_ = @Inject)
    private WorkerAppService workerAppService;

    @Operation(summary = "worker list")
    @GetMapping("/api/v1/worker")
    public ResponseDTO<PageDTO<WorkerDTO>> page(WorkerQueryParam param) {
        return ResponseDTO.<PageDTO<WorkerDTO>>builder().ok(workerAppService.page(param)).build();
    }

    /**
     * 启动worker
     */
    @Operation(summary = "启动worker")
    @PostMapping("/api/v1/worker/start")
    public ResponseDTO<Boolean> start(@NotBlank(message = "ID不能为空") @RequestParam("workerId") String workerId) {
        return ResponseDTO.<Boolean>builder().ok(workerAppService.start(workerId)).build();
    }

    /**
     * 停止worker
     */
    @Operation(summary = "停止worker")
    @PostMapping("/api/v1/worker/stop")
    public ResponseDTO<Boolean> stop(@NotBlank(message = "ID不能为空") @RequestParam("workerId") String workerId) {
        return ResponseDTO.<Boolean>builder().ok(workerAppService.stop(workerId)).build();
    }

}
