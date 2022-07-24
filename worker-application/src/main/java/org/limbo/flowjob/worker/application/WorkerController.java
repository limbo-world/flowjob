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

package org.limbo.flowjob.worker.application;


import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.clent.dto.TaskReceiveDTO;
import org.limbo.flowjob.broker.api.clent.param.TaskSubmitParam;
import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.common.exception.VerifyException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public Mono<ResponseDTO<TaskReceiveDTO>> receiveJob(@RequestBody TaskSubmitParam param) {
        TaskReceiveDTO receiveResult = new TaskReceiveDTO();
        try {
            workerService.receive(param);
            receiveResult.setAccepted(true);
        } catch (Exception e) {
            log.error("receive task error ", e);
            receiveResult.setAccepted(false);
        }
        return Mono.just(ResponseDTO.<TaskReceiveDTO>builder().data(receiveResult).build());
    }

    @GetMapping("/ping")
    public Mono<ResponseDTO<Void>> ping() {
        throw new VerifyException("xxx");
    }

}