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
import lombok.Setter;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.param.broker.JobFeedbackParam;
import org.limbo.flowjob.broker.application.schedule.ScheduleProxy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.limbo.flowjob.api.constants.rpc.HttpBrokerApi.API_JOB_FEEDBACK;
import static org.limbo.flowjob.api.constants.rpc.HttpBrokerApi.API_PLAN_INSTANCE_JOB_SCHEDULE;

/**
 * @author Devil
 * @since 2023/8/7
 */
public class JobRpcController {

    @Setter(onMethod_ = @Inject)
    private ScheduleProxy scheduleProxy;

    /**
     * api 触发对应planInstanceId下的job调度 目前只有workflow类型会用到
     */
    @Operation(summary = "触发对应job调度")
    @PostMapping(API_PLAN_INSTANCE_JOB_SCHEDULE)
    public ResponseDTO<Void> scheduleJob(@Validated @NotNull(message = "no planInstanceId") @RequestParam("planInstanceId") String planInstanceId,
                                         @Validated @NotNull(message = "no jobId") @RequestParam("jobId") String jobId) {
        scheduleProxy.scheduleJob(planInstanceId, jobId);
        return ResponseDTO.<Void>builder().ok().build();
    }

    /**
     * 任务执行反馈接口
     */
    @Operation(summary = "任务执行反馈接口")
    @PostMapping(API_JOB_FEEDBACK)
    public ResponseDTO<Void> feedback(@Validated @NotNull(message = "no job") @RequestParam("jobInstanceId") String jobInstanceId,
                                      @Valid @RequestBody JobFeedbackParam feedback) {
        scheduleProxy.feedback(jobInstanceId, feedback);
        return ResponseDTO.<Void>builder().ok().build();
    }

}
