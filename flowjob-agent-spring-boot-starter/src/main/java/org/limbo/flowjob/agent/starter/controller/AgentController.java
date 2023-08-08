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

package org.limbo.flowjob.agent.starter.controller;


import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.agent.starter.service.AgentService;
import org.limbo.flowjob.api.constants.rpc.HttpAgentApi;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.param.agent.JobSubmitParam;
import org.limbo.flowjob.api.param.agent.TaskFeedbackParam;
import org.limbo.flowjob.api.param.agent.TaskSubmitParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
@RestController("fjaAgentController")
@RequestMapping
public class AgentController extends BaseController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * 接收job
     *
     * @param param
     * @return
     */
    @PostMapping(HttpAgentApi.API_SUBMIT_JOB)
    public ResponseDTO<Boolean> receiveJob(@RequestBody JobSubmitParam param) {
        return ResponseDTO.<Boolean>builder().ok(agentService.receive(param)).build();
    }

    /**
     * task执行反馈接口
     */
    @PostMapping(HttpAgentApi.API_TASK_SUBMIT)
    public ResponseDTO<Boolean> receiveTask(@RequestBody TaskSubmitParam param) {
        return ResponseDTO.<Boolean>builder().ok(agentService.receive(param)).build();
    }

    /**
     * task执行反馈接口
     */
    @PostMapping(HttpAgentApi.API_TASK_FEEDBACK)
    public ResponseDTO<Boolean> feedback(@Validated @NotNull(message = "no taskId") @RequestParam("taskId") String taskId,
                                         @Valid @RequestBody TaskFeedbackParam feedback) {
        agentService.taskFeedback(taskId, feedback);
        return ResponseDTO.<Boolean>builder().ok(true).build();
    }

}
