package org.limbo.flowjob.tracker.admin.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerStatus;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterResult;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerAvailableResource;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
    @Transactional(rollbackFor = Throwable.class)
    public Mono<WorkerRegisterResult> register(WorkerRegisterOptionDto options) {

        // TODO 租户鉴权

        // 新增 or 更新 worker
        Worker worker = workerRepository.getWorker(options.getId());
        if (worker == null) {

            worker = createNewWorker(options);
            workerRepository.addWorker(worker);

        } else {

            worker.setIp(options.getIp());
            worker.setPort(options.getPort());
            worker.setProtocol(options.getProtocol());
            worker.setStatus(WorkerStatus.RUNNING);
            workerRepository.updateWorker(worker);

        }

        // 更新metric
        WorkerMetric metric = new WorkerMetric();
        metric.setExecutingJobs(Lists.newArrayList()); // TODO
        metric.setTimestamp(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")));
        metric.setAvailableResource(WorkerAvailableResource.from(options.getAvailableResource()));
        worker.updateMetric(metric);

        // TODO 更新worker执行器

        // 返回tracker
        WorkerRegisterResult registerResult = new WorkerRegisterResult();
        registerResult.setWorkerId(worker.getWorkerId());
        registerResult.setTrackers(null);// TODO
        return Mono.just(registerResult);
    }

    /**
     * TODO
     * @param options
     * @return
     */
    @Nonnull
    private Worker createNewWorker(WorkerRegisterOptionDto options) {
        return null;
    }

}
