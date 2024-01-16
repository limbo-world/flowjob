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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.dto.console.PlanInfoDTO;
import org.limbo.flowjob.api.dto.console.PlanVersionDTO;
import org.limbo.flowjob.api.param.console.PlanParam;
import org.limbo.flowjob.api.param.console.PlanVersionParam;
import org.limbo.flowjob.api.param.console.WorkflowPlanUpdateParam;
import org.limbo.flowjob.broker.application.service.PlanAppService;
import org.limbo.flowjob.broker.application.service.WorkflowPlanService;
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
    private PlanAppService planAppService;

    @Setter(onMethod_ = @Inject)
    private WorkflowPlanService workflowPlanService;

    /**
     * 新增工作流计划
     */
    @Operation(summary = "新增工作流计划")
    @PostMapping("/api/v1/workflow-plan/add")
    public ResponseDTO<String> add(@Validated @RequestBody PlanParam.WorkflowPlanParam options) {
        return ResponseDTO.ok(workflowPlanService.add(options));
    }


    /**
     * 更新工作流计划
     */
    @Operation(summary = "更新工作流计划")
    @PostMapping("/api/v1/workflow-plan/update")
    public ResponseDTO<String> update(@Validated @RequestBody WorkflowPlanUpdateParam param) {
        return ResponseDTO.ok(workflowPlanService.update(param));
    }


    /**
     * 更新工作量 plan 的生效版本
     */
    @Operation(summary = "更新工作量 plan 的生效版本")
    @PostMapping("/api/v1/workflow-plan/version")
    public ResponseDTO<Boolean> versionUpdate(@NotBlank(message = "ID不能为空") @RequestParam("planId") String planId,
                                              @NotBlank(message = "version不能为空") @RequestParam("version") String version) {
        return ResponseDTO.<Boolean>builder().ok(planAppService.versionUpdate(planId, version)).build();
    }


    /**
     * 获取工作流 plan 详情
     */
    @Operation(summary = "详情")
    @GetMapping("/api/v1/workflow-plan/get")
    public ResponseDTO<PlanInfoDTO.WorkflowPlanInfoDTO> get(@NotBlank(message = "ID不能为空") @RequestParam("planId") String planId) {
        return ResponseDTO.ok(workflowPlanService.getWorkflowPlan(planId));
    }


    /**
     * 获取工作流 plan 版本列表
     */
    @Operation(summary = "获取工作流 plan 版本列表")
    @GetMapping("/api/v1/workflow-plan/version/page")
    public ResponseDTO<PageDTO<PlanVersionDTO>> versionPage(PlanVersionParam param) {
        return ResponseDTO.ok(planAppService.versionPage(param));
    }


}
