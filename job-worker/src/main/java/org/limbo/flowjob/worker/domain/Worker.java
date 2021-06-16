/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.domain;

import io.rsocket.Payload;
import io.rsocket.core.RSocketClient;
import io.rsocket.loadbalance.LoadbalanceRSocketClient;
import io.rsocket.loadbalance.LoadbalanceTarget;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import org.limbo.flowjob.worker.infrastructure.JobProperties;
import org.limbo.flowjob.worker.infrastructure.WorkerConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Devil
 * @date 2021/6/10 5:32 下午
 */
public class Worker {

    private String id;

    private int queueSize;

    private RSocketRequester requester;

    private Worker() {
    }

    public static Worker create(JobProperties config) {
        Worker worker = new Worker();
        worker.id = ""; // todo
        worker.queueSize = config.getQueueSize();
        String[] ipHost = config.getTrackerAddress().split(":");
        worker.requester = RSocketRequester.builder()
                .setupRoute("api.sdk.register")
                .tcp(ipHost[0], Integer.parseInt(ipHost[1]));

        worker.start();

        return worker;
    }

    /**
     * 启动
     */
    public void start() {
        // 注册

        // 心跳
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                heartBeat();
            }
        }, 200);
    }

    /**
     * 发送心跳
     */
    public void heartBeat() {

    }

//    /**
//     * worker 服务启动
//     */
//    public void start() {
//
//        Flux<List<LoadbalanceTarget>> producer = Flux.interval(Duration.ofSeconds(5))
//                .log()
//                .map(i -> {
//                    List<LoadbalanceTarget> targets = new ArrayList<>();
//                    for (TrackerAddress trackerAddress : trackerAddresses) {
//                        targets.add(LoadbalanceTarget.from(trackerAddress.getIp() + ":" + trackerAddress,
//                                TcpClientTransport.create(trackerAddress.getIp(), trackerAddress.getPort())
//                        ));
//                    }
//                    return targets;
//                });
//
//        RSocketClient client = LoadbalanceRSocketClient.builder(producer).roundRobinLoadbalanceStrategy().build();
//        Payload test = DefaultPayload.create("test");
//        client.requestResponse(Mono.just(DefaultPayload.create("test"))).doOnSubscribe(s -> System.out.println("Executing Request"))
//                .doOnNext(
//                        d -> {
//                            System.out.println("Received response data " + d.getDataUtf8());
//                            d.release();
//                        })
//                .repeat(10)
//                .blockLast();
//
//    }

}
