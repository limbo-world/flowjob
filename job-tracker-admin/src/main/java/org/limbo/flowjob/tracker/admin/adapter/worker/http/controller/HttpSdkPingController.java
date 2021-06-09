package org.limbo.flowjob.tracker.admin.adapter.worker.http.controller;

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
@RestController
@RequestMapping("/api/sdk/ping")
public class HttpSdkPingController {

    @Autowired
    private JobTracker tracker;


    @GetMapping
    public Mono<String> ping() {
        return Mono.just("pong~" + tracker);
    }


}
