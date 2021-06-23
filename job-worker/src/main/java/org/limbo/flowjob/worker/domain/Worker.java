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

import io.rsocket.core.RSocketClient;
import org.limbo.flowjob.tracker.commons.dto.Response;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerHeartbeatOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerResourceDto;
import org.limbo.flowjob.worker.infrastructure.JobProperties;
import org.limbo.utils.JacksonUtils;
import org.limbo.utils.UUIDUtils;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Devil
 * @date 2021/6/10 5:32 下午
 */
public class Worker {

    private String id;

    private RSocketRequester requester;

    private WorkerResource resource;

    private Worker() {
    }

    public static Worker create(JobProperties config, RSocketStrategies strategies) {
        Worker worker = new Worker();
        worker.id = UUIDUtils.randomID();
        worker.resource = WorkerResource.create(config.getQueueSize());

        String[] ipHost = config.getTrackerAddress().split(":");

        worker.requester = RSocketRequester.builder()
                .setupRoute("api.worker.connect")
                .rsocketStrategies(strategies)
                .tcp(ipHost[0], Integer.parseInt(ipHost[1]));

        worker.start();

        return worker;
    }

    /**
     * 启动
     */
    public void start() {
        // 注册
        register();
        // 心跳
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                heartBeat();
            }
        }, 200, 3000);
    }

    /**
     * 向tracker注册
     */
    private void register() {
        WorkerResourceDto resourceDto = new WorkerResourceDto();
        resourceDto.setAvailableCpu(resource.getAvailableCpu());
        resourceDto.setAvailableRAM(resource.getAvailableRAM());
        resourceDto.setAvailableQueueLimit(resource.getAvailableQueueLimit());

        WorkerRegisterOptionDto registerOptionDto = new WorkerRegisterOptionDto();
        registerOptionDto.setId(id);
        registerOptionDto.setAvailableResource(resourceDto);

        requester.route("api.worker.register")
                .data(registerOptionDto)
                .retrieveMono(Response.class)
                .subscribe(response -> System.out.println(JacksonUtils.toJSONString(response)),
                        System.err::println, () -> {
                        });
    }

    /**
     * 发送心跳
     */
    private void heartBeat() {
        WorkerResourceDto resourceDto = new WorkerResourceDto();
        resourceDto.setAvailableCpu(resource.getAvailableCpu());
        resourceDto.setAvailableRAM(resource.getAvailableRAM());
        resourceDto.setAvailableQueueLimit(resource.getAvailableQueueLimit());

        WorkerHeartbeatOptionDto heartbeatOptionDto = new WorkerHeartbeatOptionDto();
        heartbeatOptionDto.setAvailableResource(resourceDto);

        requester.route("api.worker.heartbeat")
                .data(heartbeatOptionDto)
                .retrieveMono(Response.class)
                .subscribe(response -> System.out.println(JacksonUtils.toJSONString(response)),
                        System.err::println, () -> {
                        });
    }


    public void submit() {

    }

}
