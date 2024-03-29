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
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.dto.console.PlanDTO;
import org.limbo.flowjob.api.dto.console.PlanInfoDTO;
import org.limbo.flowjob.api.dto.console.PlanVersionDTO;
import org.limbo.flowjob.api.param.console.PlanParam;
import org.limbo.flowjob.api.param.console.PlanQueryParam;
import org.limbo.flowjob.api.param.console.PlanVersionParam;
import org.limbo.flowjob.broker.application.service.PlanAppService;
import org.limbo.flowjob.broker.core.meta.info.PlanRepository;
import org.limbo.flowjob.broker.core.meta.processor.PlanInstanceProcessor;
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
public class PlanController {

    @Setter(onMethod_ = @Inject)
    private PlanAppService planAppService;

    @Setter(onMethod_ = @Inject)
    private PlanRepository planRepository;

    @Setter(onMethod_ = @Inject)
    private PlanInstanceProcessor processor;

    /**
     * 新增计划
     */
    @Operation(summary = "新增计划")
    @PostMapping("/api/v1/plan/add")
    public ResponseDTO<String> add(@Validated @RequestBody PlanParam.NormalPlanParam options) {
        return ResponseDTO.<String>builder()
                .ok(planAppService.add(options))
                .build();
    }

    /**
     * 替换计划
     */
    @Operation(summary = "修改计划")
    @Parameters({
            @Parameter(name = "planId", in = ParameterIn.PATH, description = "计划ID")
    })
    @PostMapping("/api/v1/plan/update")
    public ResponseDTO<String> update(@NotBlank(message = "ID不能为空") @RequestParam("planId") String planId,
                                      @Validated @RequestBody PlanParam.NormalPlanParam options) {
        return ResponseDTO.<String>builder()
                .ok(planAppService.update(planId, options))
                .build();
    }

    /**
     * 详情
     */
    @Operation(summary = "详情")
    @GetMapping("/api/v1/plan/get")
    public ResponseDTO<PlanInfoDTO.NormalPlanInfoDTO> get(@NotBlank(message = "ID不能为空") @RequestParam("planId") String planId) {
        return ResponseDTO.<PlanInfoDTO.NormalPlanInfoDTO>builder().ok(planAppService.get(planId)).build();
    }


    /**
     * 启动计划
     */
    @Operation(summary = "启动计划")
    @PostMapping("/api/v1/plan/start")
    public ResponseDTO<Boolean> start(@NotBlank(message = "ID不能为空") @RequestParam("planId") String planId) {
        return ResponseDTO.<Boolean>builder().ok(planAppService.start(planId)).build();
    }

    /**
     * 停止计划
     */
    @Operation(summary = "停止计划")
    @PostMapping("/api/v1/plan/stop")
    public ResponseDTO<Boolean> stop(@NotBlank(message = "ID不能为空") @RequestParam("planId") String planId) {
        return ResponseDTO.<Boolean>builder().ok(planAppService.stop(planId)).build();
    }

    /**
     * 计划列表
     */
    @Operation(summary = "计划列表")
    @GetMapping("/api/v1/plan/page")
    public ResponseDTO<PageDTO<PlanDTO>> page(PlanQueryParam param) {
        return ResponseDTO.<PageDTO<PlanDTO>>builder().ok(planAppService.page(param)).build();
    }

    /**
     * 版本列表
     */
    @Operation(summary = "版本列表")
    @GetMapping("/api/v1/plan/version/page")
    public ResponseDTO<PageDTO<PlanVersionDTO>> versionPage(PlanVersionParam param) {
        return ResponseDTO.<PageDTO<PlanVersionDTO>>builder().ok(planAppService.versionPage(param)).build();
    }

    /**
     * 版本修改
     */
    @Operation(summary = "版本修改")
    @PostMapping("/api/v1/plan/version")
    public ResponseDTO<Boolean> versionUpdate(@NotBlank(message = "ID不能为空") @RequestParam("planId") String planId,
                                              @NotBlank(message = "version不能为空") @RequestParam("version") String version) {
        return ResponseDTO.<Boolean>builder().ok(planAppService.versionUpdate(planId, version)).build();
    }

}
