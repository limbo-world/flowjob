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
import org.limbo.flowjob.api.ResponseDTO;
import org.limbo.flowjob.api.remote.dto.WorkerRegisterDTO;
import org.limbo.flowjob.api.remote.param.TaskFeedbackParam;
import org.limbo.flowjob.api.remote.param.WorkerHeartbeatParam;
import org.limbo.flowjob.api.remote.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.application.component.schedule.ScheduleStrategy;
import org.limbo.flowjob.broker.application.service.TaskService;
import org.limbo.flowjob.broker.application.service.WorkerService;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanEntityRepo;
import org.limbo.flowjob.common.constants.MsgConstants;
import org.limbo.flowjob.common.constants.TriggerType;
import org.limbo.flowjob.common.exception.VerifyException;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author Brozen
 * @since 2021-06-10
 */
@Tag(name = "worker remote rpc")
@RestController
public class WorkerRpcController {

    @Setter(onMethod_ = @Inject)
    private WorkerService workerService;

    @Setter(onMethod_ = @Inject)
    private TaskService taskService;

    @Setter(onMethod_ = @Inject)
    private ScheduleStrategy scheduleStrategy;

    @Setter(onMethod_ = @Inject)
    private DomainConverter domainConverter;

    @Setter(onMethod_ = @Inject)
    private PlanEntityRepo planEntityRepo;

    /**
     * worker注册
     */
    @Operation(summary = "worker注册")
    @PostMapping("/api/v1/rpc/worker")
    public ResponseDTO<WorkerRegisterDTO> register(@Validated @RequestBody WorkerRegisterParam param) {
        return ResponseDTO.<WorkerRegisterDTO>builder().ok(workerService.register(param)).build();
    }

    /**
     * worker心跳
     */
    @Operation(summary = "worker心跳")
    @PostMapping("/api/v1/rpc/worker/{workerId}/heartbeat")
    public ResponseDTO<WorkerRegisterDTO> heartbeat(@Validated @NotNull(message = "no workerId") @PathVariable("workerId") String workerId,
                                                    @Valid @RequestBody WorkerHeartbeatParam heartbeatOption) {
        return ResponseDTO.<WorkerRegisterDTO>builder().ok(workerService.heartbeat(workerId, heartbeatOption)).build();
    }

    /**
     * api 触发对应plan
     */
    @Operation(summary = "触发对应job调度")
    @PostMapping("/api/v1/rpc/worker/plan/{planId}/schedule")
    public ResponseDTO<Void> scheduleJob(@Validated @NotNull(message = "no planId") @PathVariable("planId") String planId) {
        PlanEntity planEntity = planEntityRepo.findById(planId).orElseThrow(VerifyException.supplier(MsgConstants.CANT_FIND_PLAN + planId));
        Plan plan = domainConverter.toPlan(planEntity);
        scheduleStrategy.schedule(TriggerType.API, plan, TimeUtils.currentLocalDateTime());
        return ResponseDTO.<Void>builder().ok().build();
    }

    /**
     * api 触发对应job调度
     */
    @Operation(summary = "触发对应job调度")
    @PostMapping("/api/v1/rpc/worker/plan/{planId}/instance/{planInstanceId}/job/{jobId}/schedule")
    public ResponseDTO<Void> scheduleJob(@Validated @NotNull(message = "no planId") @PathVariable("planId") String planId,
                                      @Validated @NotNull(message = "no planInstanceId") @PathVariable("planInstanceId") String planInstanceId,
                                      @Validated @NotNull(message = "no jobId") @PathVariable("jobId") String jobId) {
        scheduleStrategy.apiScheduleJob(planId, planInstanceId, jobId);
        return ResponseDTO.<Void>builder().ok().build();
    }

    /**
     * 任务执行反馈接口
     */
    @Operation(summary = "任务执行反馈接口")
    @PostMapping("/api/v1/rpc/worker/task/{taskId}/feedback")
    public ResponseDTO<Void> feedback(@Validated @NotNull(message = "no taskId") @PathVariable("taskId") String taskId,
                                      @Valid @RequestBody TaskFeedbackParam feedback) {
        taskService.taskFeedback(taskId, feedback);
        return ResponseDTO.<Void>builder().ok().build();
    }

}
