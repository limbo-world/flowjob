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

package org.limbo.flowjob.broker.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.limbo.flowjob.api.PageDTO;
import org.limbo.flowjob.api.ResponseDTO;
import org.limbo.flowjob.api.console.param.WorkerQueryParam;
import org.limbo.flowjob.api.console.vo.WorkerVO;
import org.limbo.flowjob.broker.application.service.WorkerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Tag(name = "worker console api")
@RestController
@RequestMapping("/api/v1/worker")
public class WorkerController {

    @Setter(onMethod_ = @Inject)
    private WorkerService workerService;

    @Operation(summary = "worker list")
    @GetMapping
    public ResponseDTO<PageDTO<WorkerVO>> page(WorkerQueryParam param) {
        return ResponseDTO.<PageDTO<WorkerVO>>builder().ok(workerService.page(param)).build();
    }

}
