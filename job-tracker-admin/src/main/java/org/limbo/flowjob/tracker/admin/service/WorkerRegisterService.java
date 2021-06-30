package org.limbo.flowjob.tracker.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterResult;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 应用服务层：worker注册操作
 *
 * @author Brozen
 * @since 2021-06-03
 */
@Slf4j
@Service
public class WorkerRegisterService {

    @Autowired
    private WorkerRepository workerRepository;


    /**
     * worker注册
     * @param options 注册参数
     * @return 返回所有tracker节点信息
     */
    public Mono<WorkerRegisterResult> register(WorkerRegisterOptionDto options) {

        log.info("{}", options);
        return Mono.just(new WorkerRegisterResult());
    }

}
