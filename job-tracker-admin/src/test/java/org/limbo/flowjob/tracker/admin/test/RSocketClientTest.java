package org.limbo.flowjob.tracker.admin.test;

import org.limbo.flowjob.tracker.commons.dto.worker.WorkerHeartbeatOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerResourceDto;
import org.limbo.utils.JacksonUtils;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;

/**
 * @author Brozen
 * @since 2021-06-09
 */
public class RSocketClientTest {

    public static void main(String[] args) throws InterruptedException {
        WorkerRegisterOptionDto registerOption = new WorkerRegisterOptionDto();
        registerOption.setId("test");

        RSocketRequester requester = RSocketRequester.builder()
                .rsocketStrategies(builder -> builder
                        .encoder(new Jackson2JsonEncoder(JacksonUtils.mapper))
                        .decoder(new Jackson2JsonDecoder()))
                .setupRoute("api.worker.connect")
                .tcp("localhost", 8082);

        requester.route("api.worker.register")
                .data(registerOption)
                .retrieveMono(String.class)
                .subscribe(System.out::println, System.err::println, () -> {
                    System.out.println("complete");
                });

        requester.route("api.worker.register")
                .data(registerOption)
                .retrieveMono(String.class)
                .subscribe(System.out::println, System.err::println, () -> {
                    System.out.println("complete");
                });

        WorkerHeartbeatOptionDto heartbeatOption = new WorkerHeartbeatOptionDto();
        WorkerResourceDto resource = new WorkerResourceDto();
        resource.setAvailableCpu(2);
        resource.setAvailableRAM(4);
        heartbeatOption.setAvailableResource(resource);
        requester.route("api.worker.heartbeat")
                .data(heartbeatOption)
                .retrieveMono(String.class)
                .subscribe(System.out::println, System.err::println, () -> {});

        Thread.sleep(10000L);
    }

}
