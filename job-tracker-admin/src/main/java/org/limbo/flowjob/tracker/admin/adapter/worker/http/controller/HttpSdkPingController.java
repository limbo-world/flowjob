package org.limbo.flowjob.tracker.admin.adapter.worker.http.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@Tag(name = "Ping测试控制器")
@RestController
@RequestMapping("/api/sdk/ping")
public class HttpSdkPingController {

    @Autowired
    private JobTracker tracker;


    @Operation(summary = "ping测试接口")
    @GetMapping
    public Mono<String> ping() {
        return Mono.just("pong~" + tracker);
    }


}
