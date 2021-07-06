package org.limbo.flowjob.tracker.admin.adapter.worker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.limbo.flowjob.tracker.admin.service.WorkerRegisterService;
import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

/**
 * @author Brozen
 * @since 2021-06-10
 */
@Tag(name = "worker相关接口")
@RestController
@RequestMapping("/api/v1/worker")
public class WorkerController {

    @Autowired
    private WorkerRegisterService registerService;

    /**
     * worker注册
     */
    @Operation(summary = "worker注册")
    @PostMapping
    public Mono<ResponseDto<WorkerRegisterResult>> register(@Validated @RequestBody Mono<WorkerRegisterOptionDto> options) {
        return options.map(opt -> registerService.register(opt))
                .flatMap(result -> result.map(r -> ResponseDto.<WorkerRegisterResult>builder().ok(r).build()));
    }

}
