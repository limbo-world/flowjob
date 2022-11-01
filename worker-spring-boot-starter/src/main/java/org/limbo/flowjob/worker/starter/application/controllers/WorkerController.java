/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.worker.starter.application.controllers;


import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.api.param.TaskSubmitParam;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.worker.starter.application.services.WorkerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
@RestController("fjwWorkerController")
@RequestMapping("/api/v1")
public class WorkerController extends BaseController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    /**
     * 接收任务。
     * Worker 会检测剩余可用资源量，如资源不足则直接拒绝接收任务。
     * Worker 中如有空闲工作线程，则会立即执行任务；如果工作线程已满，则会将任务提交到积压队列，等候执行；如果积压队列已满，则接受任务失败。
     */
    @PostMapping("/task")
    public ResponseDTO<Boolean> receiveJob(@RequestBody TaskSubmitParam param) {
        return ResponseDTO.<Boolean>builder().data(workerService.receive(param)).build();
    }


}
