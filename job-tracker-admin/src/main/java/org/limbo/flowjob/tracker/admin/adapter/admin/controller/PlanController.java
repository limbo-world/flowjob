package org.limbo.flowjob.tracker.admin.adapter.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.limbo.flowjob.tracker.admin.service.plan.PlanService;
import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.limbo.flowjob.tracker.commons.dto.plan.PlanAddDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author Devil
 * @date 2021/7/14 1:57 下午
 */
@Tag(name = "作业执行相关接口")
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
     * 启动计划
     */
    @Operation(summary = "启动计划")
    @PutMapping("/{planId}/enable")
    public Mono<ResponseDto<Void>> enable(@PathVariable("planId") String planId) {
        planService.enable(planId);
        return Mono.just(ResponseDto.<Void>builder().ok().build());
    }

    /**
     * 停止计划
     */
    @Operation(summary = "停止计划")
    @PutMapping("/{planId}/disable")
    public Mono<ResponseDto<Void>> disable(@PathVariable("planId") String planId) {
        planService.disable(planId);
        return Mono.just(ResponseDto.<Void>builder().ok().build());
    }

}
