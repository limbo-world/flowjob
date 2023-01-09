package org.limbo.flowjob.broker.application.plan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.limbo.flowjob.api.console.param.PlanParam;
import org.limbo.flowjob.api.remote.dto.ResponseDTO;
import org.limbo.flowjob.broker.application.plan.service.PlanService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Tag(name = "作业执行执行计划")
@RestController
@RequestMapping("/api/v1/plan")
public class PlanController {

    @Inject
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

}
