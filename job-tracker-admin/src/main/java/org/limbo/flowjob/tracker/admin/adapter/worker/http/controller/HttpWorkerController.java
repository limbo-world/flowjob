package org.limbo.flowjob.tracker.admin.adapter.worker.http.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.utils.web.Response;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Brozen
 * @since 2021-06-10
 */
@Tag(name = "worker相关接口")
@RestController
@RequestMapping("/api/sdk/worker")
public class HttpWorkerController {

    @Operation(summary = "worker注册")
    @PutMapping
    public Response<String> register(@RequestBody WorkerRegisterOptionDto options) {
        return Response.ok(options.toString());
    }

}
