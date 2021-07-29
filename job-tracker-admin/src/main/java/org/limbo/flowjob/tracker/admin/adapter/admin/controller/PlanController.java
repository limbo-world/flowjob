package org.limbo.flowjob.tracker.admin.adapter.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.limbo.flowjob.tracker.admin.service.plan.PlanService;
import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanAddDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanReplaceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Tag(name = "作业执行执行计划")
@RestController
@RequestMapping("/api/admin/v1/plan")
public class PlanController {

    @Autowired
    private PlanService planService;

    /**
     * 新增计划
     */
    @Operation(summary = "新增计划")
    @PostMapping
    public Mono<ResponseDto<String>> add(@Validated @RequestBody Mono<PlanAddDto> options) {
        return options.map(opt -> ResponseDto.<String>builder().ok(planService.add(opt)).build());
    }

    /**
     * 替换计划
     */
    @Operation(summary = "修改计划")
    @Parameters({
            @Parameter(name = "planId", in = ParameterIn.PATH, description = "计划ID")
    })
    @PutMapping("/{planId}")
    public Mono<ResponseDto<Void>> replace(@NotBlank(message = "ID不能为空") @PathVariable("planId") String planId,
                                          @Validated @RequestBody PlanReplaceDto dto) {
        planService.replace(planId, dto);
        return Mono.just(ResponseDto.<Void>builder().ok().build());
    }

    /**
     * 启动计划
     */
    @Operation(summary = "启动计划")
    @PutMapping("/{planId}/start")
    public Mono<ResponseDto<Void>> start(@PathVariable("planId") String planId) {
        planService.start(planId);
        return Mono.just(ResponseDto.<Void>builder().ok().build());
    }

    /**
     * 停止计划
     */
    @Operation(summary = "停止计划")
    @PutMapping("/{planId}/stop")
    public Mono<ResponseDto<Void>> stop(@PathVariable("planId") String planId) {
        planService.stop(planId);
        return Mono.just(ResponseDto.<Void>builder().ok().build());
    }

}
