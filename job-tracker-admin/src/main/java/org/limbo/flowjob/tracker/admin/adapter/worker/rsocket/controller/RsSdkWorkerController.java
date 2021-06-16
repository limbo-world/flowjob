package org.limbo.flowjob.tracker.admin.adapter.worker.rsocket.controller;

import org.limbo.flowjob.tracker.commons.dto.Response;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterResult;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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
    private JobTracker tracker;

    /**
     * worker注册接口
     * @param registerOption worker注册参数
     * @return 注册结果响应
     */
    @MessageMapping("sdk.worker.register")
    public Mono<Response<WorkerRegisterResult>> register(@Payload WorkerRegisterOptionDto registerOption) {
        return Mono.just(Response.<WorkerRegisterResult>builder().ok().build());
    }


}
