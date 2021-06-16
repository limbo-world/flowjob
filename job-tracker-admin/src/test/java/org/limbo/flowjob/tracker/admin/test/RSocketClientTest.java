package org.limbo.flowjob.tracker.admin.test;

import org.springframework.messaging.rsocket.RSocketRequester;

/**
 * @author Brozen
 * @since 2021-06-09
 */
public class RSocketClientTest {

    public static void main(String[] args) throws InterruptedException {
        RSocketRequester requester = RSocketRequester.builder()
                .setupRoute("api.sdk.register")
                .tcp("localhost", 8082);
//        requester.route("api.sdk.ping")
//                .send().subscribe();
        requester.route("api.sdk.ping")
                .data("hello")
                .retrieveMono(String.class)
                .subscribe(System.out::println, System.err::println, () -> {});

        requester.route("api.sdk.ping")
                .data("hello")
                .retrieveMono(String.class)
                .subscribe(System.out::println, System.err::println, () -> {});

        Thread.sleep(10000L);
    }

}
