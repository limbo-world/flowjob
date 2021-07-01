package org.limbo.flowjob.tracker.admin.adapter.worker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.limbo.flowjob.tracker.admin.service.WorkerRegisterService;
import org.limbo.flowjob.tracker.commons.dto.Response;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Brozen
 * @since 2021-06-10
 */
@Tag(name = "worker相关接口")
@RestController
@RequestMapping("/api/sdk/worker")
public class WorkerController {

    @Autowired
    private WorkerRegisterService registerService;

    /**
     * worker注册
     */
    @Operation(summary = "worker注册")
    @PutMapping
    public Mono<Response<WorkerRegisterResult>> register(@RequestBody WorkerRegisterOptionDto options) {
        return registerService.register(options)
                .map(result -> Response.<WorkerRegisterResult>builder().ok(result).build());
    }

}
