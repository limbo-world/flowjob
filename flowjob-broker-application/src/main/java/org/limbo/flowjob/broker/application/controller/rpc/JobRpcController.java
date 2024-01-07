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
import org.limbo.flowjob.api.dto.broker.AvailableWorkerDTO;
import org.limbo.flowjob.api.param.broker.JobFeedbackParam;
import org.limbo.flowjob.broker.application.service.WorkerAppService;
import org.limbo.flowjob.broker.core.meta.processor.PlanInstanceProcessor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static org.limbo.flowjob.api.constants.rpc.HttpBrokerApi.*;

/**
 * @author Devil
 * @since 2023/8/7
 */
@Tag(name = "job remote rpc")
@RestController
public class JobRpcController {

    @Setter(onMethod_ = @Inject)
    private PlanInstanceProcessor processor;

    @Setter(onMethod_ = @Inject)
    private WorkerAppService workerAppService;

    /**
     * job开始执行反馈
     */
    @Operation(summary = "job开始执行反馈")
    @PostMapping(API_JOB_EXECUTING)
    public ResponseDTO<Boolean> executing(@Validated @NotNull(message = "no agentId") @RequestParam("agentId") String agentId,
                                          @Validated @NotNull(message = "no jobInstanceId") @RequestParam("jobInstanceId") String jobInstanceId) {
        boolean result = processor.jobExecuting(agentId, jobInstanceId);
        return ResponseDTO.<Boolean>builder().ok(result).build();
    }

    /**
     * job执行上报
     */
    @Operation(summary = "job执行上报")
    @PostMapping(API_JOB_REPORT)
    public ResponseDTO<Boolean> report(@Validated @NotNull(message = "no jobInstanceId") @RequestParam("jobInstanceId") String jobInstanceId) {
        boolean result = processor.jobReport(jobInstanceId);
        return ResponseDTO.<Boolean>builder().ok(result).build();
    }

    /**
     * 任务执行反馈接口
     */
    @Operation(summary = "任务执行反馈接口")
    @PostMapping(API_JOB_FEEDBACK)
    public ResponseDTO<Boolean> feedback(@Validated @NotNull(message = "no job") @RequestParam("jobInstanceId") String jobInstanceId,
                                         @Valid @RequestBody JobFeedbackParam feedback) {
        processor.feedback(jobInstanceId, feedback);
        return ResponseDTO.<Boolean>builder().ok(true).build();
    }

    /**
     * 任务可执行worker
     *
     * @param jobInstanceId  任务id
     * @param filterExecutor 是否基于执行器过滤
     * @param filterTag      是否基于标签过滤
     * @param filterResource 是否基于资源过滤
     * @param lbSelect       是否基于负载返回合适的一个
     * @return
     */
    @Operation(summary = "任务可执行worker")
    @GetMapping(API_JOB_FILTER_WORKER)
    public ResponseDTO<List<AvailableWorkerDTO>> filterJobWorkers(@Validated @NotNull(message = "no job") @RequestParam("jobInstanceId") String jobInstanceId,
                                                                  @RequestParam(value = "filterExecutor", required = false) boolean filterExecutor,
                                                                  @RequestParam(value = "filterTag", required = false) boolean filterTag,
                                                                  @RequestParam(value = "filterResource", required = false) boolean filterResource,
                                                                  @RequestParam(value = "lbSelect", required = false) boolean lbSelect) {
        List<AvailableWorkerDTO> workerDTOS = workerAppService.filterJobWorkers(jobInstanceId, filterExecutor, filterTag, filterResource, lbSelect);
        return ResponseDTO.<List<AvailableWorkerDTO>>builder().ok(workerDTOS).build();
    }

}
