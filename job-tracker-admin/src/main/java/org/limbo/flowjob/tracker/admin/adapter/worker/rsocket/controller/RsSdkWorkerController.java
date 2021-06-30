package org.limbo.flowjob.tracker.admin.adapter.worker.rsocket.controller;

import org.limbo.flowjob.tracker.admin.service.WorkerRegisterService;
import org.limbo.flowjob.tracker.commons.dto.Response;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerHeartbeatOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterResult;
import org.limbo.utils.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

/**
 * @author Brozen
 * @since 2021-06-16
 */
@Controller
public class RsSdkWorkerController {

    /**
     * 当前JobTracker
     */
    @Autowired
    private WorkerRegisterService workerRegisterService;

    /**
     * 建立连接
     */
    @ConnectMapping("api.worker.connect")
    public Mono<Void> connect() {
        System.out.println("Connected");
        return Mono.empty();
    }

    /**
     * worker注册接口
     * @param registerOption worker注册参数
     * @return 注册结果响应
     */
    @MessageMapping("api.worker.register")
    public Mono<Response<WorkerRegisterResult>> register(@Payload WorkerRegisterOptionDto registerOption) {
        System.out.println(JacksonUtils.toJSONString(registerOption));
        return workerRegisterService.register(registerOption)
                .map(result -> Response.<WorkerRegisterResult>builder().ok().data(result).build());
    }

    @MessageMapping("api.worker.heartbeat")
    public Mono<Response<Void>> heartbeat(@Payload WorkerHeartbeatOptionDto heartbeatOption) {
        System.out.println(JacksonUtils.toJSONString(heartbeatOption));
        return Mono.just(Response.<Void>builder().ok().message("hea").build());
    }


}
