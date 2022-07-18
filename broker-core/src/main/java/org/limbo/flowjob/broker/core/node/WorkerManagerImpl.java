package org.limbo.flowjob.broker.core.node;

import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;

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
    public Worker registerWorker(Worker worker) {
        workerRepository.addWorker(worker);
        return worker;
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
    public Worker unregisterWorker(String workerId) {
        Worker worker = workerRepository.getWorker(workerId);
        workerRepository.removeWorker(workerId);
        return worker;
    }
}
