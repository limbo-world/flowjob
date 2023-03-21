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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.limbo.flowjob.api.PageDTO;
import org.limbo.flowjob.api.ResponseDTO;
import org.limbo.flowjob.api.console.param.PlanParam;
import org.limbo.flowjob.api.console.param.PlanQueryParam;
import org.limbo.flowjob.api.console.vo.PlanVO;
import org.limbo.flowjob.broker.application.service.PlanService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Tag(name = "plan console api")
@RestController
@RequestMapping("/api/v1/plan")
public class PlanController {

    @Setter(onMethod_ = @Inject)
    private PlanService planService;

    /**
     * 新增计划
     */
    @Operation(summary = "新增计划")
    @PostMapping
    public ResponseDTO<String> add(@Validated @RequestBody PlanParam options) {
        return ResponseDTO.<String>builder()
                .ok(planService.save(null, options))
                .build();
    }

    /**
     * 替换计划
     */
    @Operation(summary = "修改计划")
    @Parameters({
            @Parameter(name = "planId", in = ParameterIn.PATH, description = "计划ID")
    })
    @PutMapping("/{planId}")
    public ResponseDTO<String> replace(@NotBlank(message = "ID不能为空") @PathVariable("planId") String planId,
                                           @Validated @RequestBody PlanParam options) {
        return ResponseDTO.<String>builder()
                .ok(planService.save(planId, options))
                .build();
    }

    /**
     * 启动计划
     */
    @Operation(summary = "启动计划")
    @PutMapping("/{planId}/start")
    public ResponseDTO<Boolean> start(@PathVariable("planId") String planId) {
        return ResponseDTO.<Boolean>builder().ok(planService.start(planId)).build();
    }

    /**
     * 停止计划
     */
    @Operation(summary = "停止计划")
    @PutMapping("/{planId}/stop")
    public ResponseDTO<Boolean> stop(@PathVariable("planId") String planId) {
        return ResponseDTO.<Boolean>builder().ok(planService.stop(planId)).build();
    }

    /**
     * 计划列表
     */
    @Operation(summary = "计划列表")
    @GetMapping
    public ResponseDTO<PageDTO<PlanVO>> page(PlanQueryParam param) {
        return ResponseDTO.<PageDTO<PlanVO>>builder().ok(planService.page(param)).build();
    }

}
