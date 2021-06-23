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

package org.limbo.flowjob.worker.adapter;


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

/**
 * @author Devil
 * @date 2021/6/10 4:23 下午
 */
@Controller
public class RSocketController {

    /**
     * 任务下发
     * @param greet
     * @return
     */
    @MessageMapping("api.sdk.job.submit")
    public Mono<String> jobSubmit(@Payload String greet) {
        System.out.println(greet);
        return Mono.just("pong~~");
    }

    /**
     * 任务状态查询
     * @param greet
     * @return
     */
    @MessageMapping("api.sdk.job.state")
    public Mono<String> jobState(@Payload String greet) {
        System.out.println(greet);
        return Mono.just("pong~~");
    }

    /**
     * 队列动态扩缩容
     * @param greet
     * @return
     */
    @MessageMapping("api.sdk.queue.resize")
    public Mono<String> queueResize(@Payload String greet) {
        System.out.println(greet);
        return Mono.just("pong~~");
    }

    /**
     * tracker主从切换通知
     * @param greet
     * @return
     */
    @MessageMapping("api.sdk.tracker.switch")
    public Mono<String> trackerSwitch(@Payload String greet) {
        System.out.println(greet);
        return Mono.just("pong~~");
    }

}
