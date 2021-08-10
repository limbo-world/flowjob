//package org.limbo.flowjob.tracker.core.tracker.bak;
//
//import com.alipay.sofa.jraft.entity.PeerId;
//import lombok.extern.slf4j.Slf4j;
//import org.limbo.flowjob.tracker.core.raft.ElectionNode;
//import org.limbo.flowjob.tracker.core.raft.ElectionNodeOptions;
//import org.limbo.flowjob.tracker.core.raft.StateListener;
//import org.limbo.flowjob.tracker.core.tracker.JobTracker;
//import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
//import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
//import reactor.core.publisher.Mono;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// * 选举 tracker
// *
// * @author Devil
// * @since 2021/8/4
// */
//@Slf4j
//public class ElectionJobTracker extends ReactorJobTrackerLifecycle implements JobTracker {
//
//    /**
//     * 当前JobTracker状态
//     */
//    private final AtomicReference<JobTrackerState> state;
//
//    /**
//     * 用户管理worker，实现WorkerManager的相关功能
//     */
//    private final WorkerRepository workerRepository;
//
//    /**
//     * jraft 节点
//     */
//    private ElectionNode electionNode;
//    /**
//     * jraft 配置
//     */
//    private ElectionNodeOptions electionOpts;
//
//    public ElectionJobTracker(ElectionNodeOptions electionOpts, WorkerRepository workerRepository) {
//        this.workerRepository = workerRepository;
//        this.state = new AtomicReference<>(JobTrackerState.INIT);
//        this.electionOpts = electionOpts;
//    }
//
//    /**
//     * {@inheritDoc}<br/>
//     * 这里也许会要进行一些资源初始化？
//     *
//     * @return
//     */
//    @PostConstruct
//    @Override
//    public DisposableJobTracker start() {
//        // 重复启动检测
//        if (!state.compareAndSet(JobTrackerState.INIT, JobTrackerState.STARTING)) {
//            throw new IllegalStateException("JobTracker already running!");
//        }
//
//        // 生成DisposableJobTracker，触发BEFORE_START事件
//        DisposableJobTrackerBind disposable = new DisposableJobTrackerBind();
//        triggerBeforeStart(disposable);
//
//        // 启动过程中被中断
//        if (disposable.isDisposed()) {
//            return disposable;
//        }
//
//        // 设置状态，成功则触发AFTER_START事件
//        if (!state.compareAndSet(JobTrackerState.STARTING, JobTrackerState.STARTED)) {
//            log.warn("Set JobTracker state to STARTED failed, maybe stop() is called in a async thread?");
//        } else {
//            triggerAfterStart(disposable);
//        }
//
//        // 选举
//        election();
//
//        return disposable;
//    }
//
//    /**
//     * 选举
//     */
//    private void election() {
//        electionNode = new ElectionNode();
//        electionNode.addStateListener(new StateListener() {
//            @Override
//            public void onLeaderStart(long newTerm) {
//
//            }
//
//            @Override
//            public void onLeaderStop(long oldTerm) {
//
//            }
//
//            @Override
//            public void onStartFollowing(PeerId newLeaderId, long newTerm) {
//
//            }
//
//            @Override
//            public void onStopFollowing(PeerId oldLeaderId, long oldTerm) {
//
//            }
//        });
//        electionNode.init(electionOpts);
//    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @return
//     */
//    @PreDestroy
//    @Override
//    public void stop() {
//
//        JobTrackerState currState = state.get();
//        if (currState != JobTrackerState.STARTING && currState != JobTrackerState.STARTED) {
//            throw new IllegalStateException("JobTracker is not running!");
//        }
//
//        if (state.compareAndSet(currState, JobTrackerState.STOPPING)) {
//            throw new IllegalStateException("JobTracker already stopped!");
//        }
//
//        // 触发BEFORE_STOP事件
//        triggerBeforeStop(this);
//
//        // 更新状态，并在更新成功时触发AFTER_STOP事件
//        if (!state.compareAndSet(JobTrackerState.STOPPING, JobTrackerState.TERMINATED)) {
//            log.warn("Set JobTracker state to TERMINATED failed!");
//        } else {
//            triggerAfterStop(this);
//        }
//
//    }
//
//
//    /**
//     * {@inheritDoc}
//     *
//     * @return
//     */
//    @Override
//    public boolean isRunning() {
//        return this.state.get() == JobTrackerState.STARTED;
//    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @return
//     */
//    public boolean isStopped() {
//        JobTrackerState state = this.state.get();
//        return state == JobTrackerState.STOPPING || state == JobTrackerState.TERMINATED;
//    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @return {@inheritDoc}
//     */
//    @Override
//    public JobTrackerLifecycle lifecycle() {
//        return this;
//    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @param worker worker节点
//     * @return
//     */
//    @Override
//    public Mono<Worker> registerWorker(Worker worker) {
//        workerRepository.addWorker(worker);
//        return Mono.just(worker);
//    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @return
//     */
//    @Override
//    public List<Worker> availableWorkers() {
//        return workerRepository.availableWorkers();
//    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @param workerId worker id。
//     * @return
//     */
//    @Override
//    public Mono<Worker> unregisterWorker(String workerId) {
//        Worker worker = workerRepository.getWorker(workerId);
//        workerRepository.removeWorker(workerId);
//        return Mono.just(worker);
//    }
//
//    /**
//     * JobTracker的状态
//     */
//    enum JobTrackerState {
//
//        INIT,
//
//        STARTING,
//
//        STARTED,
//
//        STOPPING,
//
//        TERMINATED
//
//    }
//
//
//    /**
//     * {@inheritDoc}
//     * 用于关闭JobTracker的实现。
//     */
//    class DisposableJobTrackerBind implements DisposableJobTracker {
//
//        /**
//         * {@inheritDoc}
//         *
//         * @return
//         */
//        @Override
//        public JobTracker jobTracker() {
//            return ElectionJobTracker.this;
//        }
//
//        /**
//         * {@inheritDoc}
//         */
//        @Override
//        public void dispose() {
//            ElectionJobTracker jobTracker = ElectionJobTracker.this;
//            if (jobTracker.isStopped()) {
//                return;
//            }
//
//            jobTracker.stop();
//        }
//
//        /**
//         * {@inheritDoc}
//         *
//         * @return
//         */
//        @Override
//        public boolean isDisposed() {
//            return ElectionJobTracker.this.isStopped();
//        }
//    }
//}
