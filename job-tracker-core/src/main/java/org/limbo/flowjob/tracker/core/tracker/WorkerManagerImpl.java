package org.limbo.flowjob.tracker.core.tracker;

import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author Devil
 * @since 2021/8/9
 */
public class WorkerManagerImpl implements WorkerManager {
    /**
     * 用户管理worker，实现WorkerManager的相关功能
     */
    private final WorkerRepository workerRepository;

    public WorkerManagerImpl(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    /**
     * {@inheritDoc}
     *
     * @param worker worker节点
     * @return
     */
    @Override
    public Mono<Worker> registerWorker(Worker worker) {
        workerRepository.addWorker(worker);
        return Mono.just(worker);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<Worker> availableWorkers() {
        return workerRepository.availableWorkers();
    }

    /**
     * {@inheritDoc}
     *
     * @param workerId worker id。
     * @return
     */
    @Override
    public Mono<Worker> unregisterWorker(String workerId) {
        Worker worker = workerRepository.getWorker(workerId);
        workerRepository.removeWorker(workerId);
        return Mono.just(worker);
    }
}
