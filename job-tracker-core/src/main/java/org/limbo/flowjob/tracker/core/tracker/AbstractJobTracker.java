package org.limbo.flowjob.tracker.core.tracker;

import org.limbo.flowjob.tracker.core.tracker.worker.WorkerDO;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;

/**
 * @author Brozen
 * @since 2021-06-01
 */
public abstract class AbstractJobTracker extends ReactorJobTrackerLifecycle implements JobTracker {

    /**
     * 用户管理worker，实现WorkerManager的相关功能
     */
    private WorkerRepository workerRepository;

    /**
     * 用于触发Worker注册事件
     */
    private Sinks.Many<WorkerEvent> workerEventSink;


    public AbstractJobTracker(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
        this.workerEventSink = Sinks.many().multicast().directAllOrNothing();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public JobTrackerLifecycle lifecycle() {
        return this;
    }

    /**
     * {@inheritDoc}
     * @param worker worker节点
     * @return
     */
    @Override
    public Mono<WorkerDO> registerWorker(WorkerDO worker) {
        workerRepository.addWorker(worker);
        return Mono.just(worker);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public List<WorkerDO> availableWorkers() {
        return workerRepository.availableWorkers();
    }

    /**
     * {@inheritDoc}
     * @param workerId worker id。
     * @return
     */
    @Override
    public Mono<WorkerDO> unregisterWorker(String workerId) {
        WorkerDO worker = workerRepository.getWorker(workerId);
        workerRepository.removeWorker(workerId);
        return Mono.just(worker);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Flux<WorkerDO> onWorkerRegistered() {
        return workerEventSink.asFlux()
                .filter(e -> e.type == WorkerEventType.REGISTERED)
                .map(e -> e.worker);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Flux<WorkerDO> onWorkerUnregistered() {
        return workerEventSink.asFlux()
                .filter(e -> e.type == WorkerEventType.UNREGISTERED)
                .map(e -> e.worker);
    }


    /**
     * worker注册事件对象
     */
    public static class WorkerEvent {

        /**
         * 触发事件的worker
         */
        private WorkerDO worker;

        /**
         * 时间类型
         */
        private WorkerEventType type;

        public WorkerEvent(WorkerDO worker, WorkerEventType type) {
            this.worker = worker;
            this.type = type;
        }

    }
}
