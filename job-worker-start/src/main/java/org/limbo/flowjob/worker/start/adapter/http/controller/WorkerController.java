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


import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.limbo.flowjob.tracker.commons.dto.task.TaskDto;
import org.limbo.flowjob.tracker.commons.dto.worker.JobReceiveResult;
import org.limbo.flowjob.worker.start.application.WorkerService;
import org.limbo.utils.verifies.VerifyException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
@RestController
@RequestMapping("/api/worker/v1")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    /**
     * 接收任务 并处理
     */
    @PostMapping("/task")
    public Mono<ResponseDto<JobReceiveResult>> receiveJob(@RequestBody TaskDto dto) {
        JobReceiveResult receiveResult = new JobReceiveResult();
        try {
            workerService.receive(dto);
            receiveResult.setAccepted(true);
        } catch (Exception e) {
            log.error("receive job error ", e);
            receiveResult.setAccepted(false);
        }
        return Mono.just(ResponseDto.<JobReceiveResult>builder().data(receiveResult).build());
    }

    @GetMapping("/ping")
    public Mono<ResponseDto<Void>> ping() {
        throw new VerifyException("xxx");
    }

}
