package org.limbo.flowjob.broker.application.plan.adapter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.limbo.flowjob.broker.api.console.param.PlanAddParam;
import org.limbo.flowjob.broker.api.console.param.PlanReplaceParam;
import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.broker.application.plan.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Tag(name = "作业执行执行计划")
@RestController
@RequestMapping("/api/v1/plan")
public class PlanController {

    @Autowired
    private PlanService planService;

    /**
     * 新增计划
     */
    @Operation(summary = "新增计划")
    @PostMapping
    public ResponseDTO<String> add(@Validated @RequestBody PlanAddParam options) {
        return ResponseDTO.<String>builder()
                .ok(planService.addPlan(options))
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
    public ResponseDTO<Void> replace(@NotBlank(message = "ID不能为空") @PathVariable("planId") String planId,
                                           @Validated @RequestBody PlanReplaceParam dto) {
        planService.replace(planId, dto);
        return ResponseDTO.<Void>builder().ok().build();
    }

    /**
     * 启动计划
     */
    @Operation(summary = "启动计划")
    @PutMapping("/{planId}/start")
    public ResponseDTO<Void> start(@PathVariable("planId") String planId) {
        planService.start(planId);
        return ResponseDTO.<Void>builder().ok().build();
    }

    /**
     * 停止计划
     */
    @Operation(summary = "停止计划")
    @PutMapping("/{planId}/stop")
    public ResponseDTO<Void> stop(@PathVariable("planId") String planId) {
        planService.stop(planId);
        return ResponseDTO.<Void>builder().ok().build();
    }

}
