package org.limbo.flowjob.tracker.core.tracker;

import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
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
    public Mono<Worker> registerWorker(Worker worker) {
        workerRepository.addWorker(worker);
        return Mono.just(worker);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public List<Worker> availableWorkers() {
        return workerRepository.availableWorkers();
    }

    /**
     * {@inheritDoc}
     * @param workerId worker id。
     * @return
     */
    @Override
    public Mono<Worker> unregisterWorker(String workerId) {
        Worker worker = workerRepository.getWorker(workerId);
        workerRepository.removeWorker(workerId);
        return Mono.just(worker);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Flux<Worker> onWorkerRegistered() {
        return workerEventSink.asFlux()
                .filter(e -> e.type == WorkerEventType.REGISTERED)
                .map(e -> e.worker);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Flux<Worker> onWorkerUnregistered() {
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
        private Worker worker;

        /**
         * 时间类型
         */
        private WorkerEventType type;

        public WorkerEvent(Worker worker, WorkerEventType type) {
            this.worker = worker;
            this.type = type;
        }

    }
}
