/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.start.adapter.http.controller;


import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.limbo.flowjob.tracker.commons.dto.job.JobContextDto;
import org.limbo.flowjob.worker.start.application.WorkerService;
import org.limbo.utils.verifies.VerifyException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author Devil
 * @date 2021/6/10 4:23 下午
 */
@RestController
@RequestMapping("/api/v1/worker")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    /**
     * 接收任务 并处理
     */
    @PostMapping("/job")
    public Mono<ResponseDto<Void>> receiveJob(@RequestBody JobContextDto dto) {
        workerService.receive(dto);
        return Mono.just(ResponseDto.<Void>builder().build());
    }

    @GetMapping("/ping")
    public Mono<ResponseDto<Void>> ping() {
        throw new VerifyException("xxx");
    }

}
