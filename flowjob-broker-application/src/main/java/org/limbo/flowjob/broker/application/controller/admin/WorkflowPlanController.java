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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.limbo.flowjob.api.constants.TriggerType;
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.dto.console.PlanDTO;
import org.limbo.flowjob.api.dto.console.PlanInfoDTO;
import org.limbo.flowjob.api.dto.console.PlanVersionDTO;
import org.limbo.flowjob.api.param.console.PlanParam;
import org.limbo.flowjob.api.param.console.PlanQueryParam;
import org.limbo.flowjob.api.param.console.PlanVersionParam;
import org.limbo.flowjob.broker.application.schedule.ScheduleProxy;
import org.limbo.flowjob.broker.application.service.PlanService;
import org.limbo.flowjob.broker.core.domain.plan.Plan;
import org.limbo.flowjob.broker.core.domain.plan.PlanRepository;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Tag(name = "plan console api")
@RestController
public class WorkflowPlanController {

    @Setter(onMethod_ = @Inject)
    private PlanService planService;

    /**
     * 新增工作流计划
     */
    @Operation(summary = "新增工作流计划")
    @PostMapping("/api/v1/workflow-plan/add")
    public ResponseDTO<String> add(@Validated @RequestBody PlanParam.WorkflowPlanParam options) {
        return ResponseDTO.ok(planService.add(options));
    }


    /**
     * 替换工作流计划
     */
    @Operation(summary = "替换工作流计划")
    @Parameters({
            @Parameter(name = "planId", in = ParameterIn.PATH, description = "计划ID")
    })
    @PostMapping("/api/v1/workflow-plan/update")
    public ResponseDTO<String> update(@NotBlank(message = "ID不能为空") @RequestParam("planId") String planId,
                                      @Validated @RequestBody PlanParam.WorkflowPlanParam options) {
        return ResponseDTO.ok(planService.update(planId, options));
    }


    /**
     * 获取工作流 plan 详情
     */
    @Operation(summary = "详情")
    @GetMapping("/api/v1/workflow-plan/get")
    public ResponseDTO<PlanInfoDTO.NormalPlanInfoDTO> get(@NotBlank(message = "ID不能为空") @RequestParam("planId") String planId) {
        return ResponseDTO.<PlanInfoDTO.NormalPlanInfoDTO>builder().ok(planService.get(planId)).build();
    }



}
