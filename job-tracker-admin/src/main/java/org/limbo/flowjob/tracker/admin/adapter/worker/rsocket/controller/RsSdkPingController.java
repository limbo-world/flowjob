package org.limbo.flowjob.tracker.admin.adapter.worker.rsocket.controller;

import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

/**
 * @author Brozen
 * @since 2021-06-09
 */
@Controller
public class RsSdkPingController {


    @Autowired
    private JobTracker tracker;

    @ConnectMapping("api.sdk.register")
    public Mono<Void> connect() {
        System.out.println("Connected");
        return Mono.empty();
    }


    @MessageMapping("api.sdk.ping")
    public Mono<String> ping(@Payload String greet) {
        System.out.println(greet);
        return Mono.just("pong~~" + tracker);
    }


}
