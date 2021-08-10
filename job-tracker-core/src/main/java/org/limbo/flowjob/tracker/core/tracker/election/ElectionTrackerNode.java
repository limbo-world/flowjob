package org.limbo.flowjob.tracker.core.tracker.election;

import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.alipay.sofa.jraft.rpc.RpcServer;
import com.alipay.sofa.jraft.rpc.impl.BoltRaftRpcFactory;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.dto.tracker.TrackerNodeDto;
import org.limbo.flowjob.tracker.commons.utils.NetUtils;
import org.limbo.flowjob.tracker.core.raft.ElectionNode;
import org.limbo.flowjob.tracker.core.raft.ElectionNodeOptions;
import org.limbo.flowjob.tracker.core.raft.StateListener;
import org.limbo.flowjob.tracker.core.tracker.*;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.RpcCaller;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.ScheduleRequest;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.TrackerNodeRegisterRequest;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import reactor.core.publisher.Mono;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Devil
 * @since 2021/8/9
 */
@Slf4j
public class ElectionTrackerNode extends ReactorTrackerNodeLifecycle implements TrackerNode {

    /**
     * 提供给worker的服务port
     */
    private int port;
    /**
     * 当前JobTracker状态
     */
    private final AtomicReference<NodeState> state;

    private WorkerManager workerManager;

    private ElectionJobTrackerFactory electionJobTrackerFactory;

    /**
     * 当前的 jobTracker
     */
    private JobTracker jobTracker;

    /**
     * jraft 节点
     */
    private ElectionNode electionNode;
    /**
     * jraft 配置
     */
    private ElectionNodeOptions electionOpts;
    /**
     * 节点之间进行远程调用
     */
    private RpcCaller rpcCaller;

    private List<TrackerNodeDto> trackerNodes;

    public ElectionTrackerNode(int port, ElectionNodeOptions electionOpts, ElectionJobTrackerFactory electionJobTrackerFactory,
                               WorkerManager workerManager) {
        this.electionJobTrackerFactory = electionJobTrackerFactory;
        this.workerManager = workerManager;
        this.state = new AtomicReference<>(NodeState.INIT);
        this.electionOpts = electionOpts;
        this.port = port;

        BoltRaftRpcFactory boltRaftRpcFactory = new BoltRaftRpcFactory();
        this.rpcCaller = new RpcCaller(boltRaftRpcFactory.createRpcClient());
    }

    /**
     * {@inheritDoc}<br/>
     * 这里也许会要进行一些资源初始化？
     *
     * @return
     */
    @Override
    public DisposableTrackerNode start() {
        // 重复启动检测
        if (!state.compareAndSet(NodeState.INIT, NodeState.STARTING)) {
            throw new IllegalStateException("JobTracker already running!");
        }

        // 生成DisposableJobTracker，触发BEFORE_START事件
        DisposableTrackerNodeBind disposable = new DisposableTrackerNodeBind();
        triggerBeforeStart(disposable);

        // 启动过程中被中断
        if (disposable.isDisposed()) {
            return disposable;
        }

        // 选举
        election(disposable);

        // 设置状态，成功则触发AFTER_START事件
//        if (!state.compareAndSet(NodeState.STARTING, NodeState.STARTED)) {
//            log.warn("Set JobTracker state to STARTED failed, maybe stop() is called in a async thread?");
//        } else {
//            triggerAfterStart(disposable);
//        }

        return disposable;
    }

    /**
     * 选举
     */
    private void election(DisposableTrackerNodeBind disposable) {
        List<RpcProcessor<?>> processors = new ArrayList<>();
        // todo
        processors.add(new RpcProcessor<ScheduleRequest>() {
            @Override
            public void handleRequest(RpcContext rpcCtx, ScheduleRequest request) {

            }

            @Override
            public String interest() {
                return ScheduleRequest.class.getName();
            }
        });

        electionNode = new ElectionNode(processors);
        electionNode.addStateListener(new StateListener() {
            @Override
            public void onLeaderStart(long newTerm) {
                log.info("start Leader " + electionNode.getNode().getLeaderId());
                jobTracker = electionJobTrackerFactory.leader();

                // 选举结束触发事件，提供服务
                if (state.compareAndSet(NodeState.STARTING, NodeState.STARTED)) {
                    triggerAfterStart(disposable);
                }
            }

            // todo 状态变更
            @Override
            public void onLeaderStop(long oldTerm) {
                log.info("stop Leader " + electionNode.getNode().getLeaderId());
                jobTracker = null;
            }

            @Override
            public void onStartFollowing(PeerId newLeaderId, long newTerm) {
                log.info(electionNode.getNode().getNodeId() + " start following leader " + newLeaderId);
                jobTracker = electionJobTrackerFactory.follower(newLeaderId.getEndpoint(), rpcCaller);

                // todo 通过rpc像leader上报自己的节点信息
                TrackerNodeRegisterRequest request = new TrackerNodeRegisterRequest();
                request.setIp(NetUtils.getLocalIp());
                request.setPort(port);

                rpcCaller.invokeSync(newLeaderId.getEndpoint(), request);

                // 选举结束触发事件，提供服务
                if (state.compareAndSet(NodeState.STARTING, NodeState.STARTED)) {
                    triggerAfterStart(disposable);
                }
            }

            // todo 状态变更
            @Override
            public void onStopFollowing(PeerId oldLeaderId, long oldTerm) {
                log.info(electionNode.getNode().getNodeId() + " stop following leader " + oldLeaderId);
            }
        });
        electionNode.init(electionOpts);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @PreDestroy
    @Override
    public void stop() {

        NodeState currState = state.get();
        if (currState != NodeState.STARTING && currState != NodeState.STARTED) {
            throw new IllegalStateException("JobTracker is not running!");
        }

        if (state.compareAndSet(currState, NodeState.STOPPING)) {
            throw new IllegalStateException("JobTracker already stopped!");
        }

        // 触发BEFORE_STOP事件
        triggerBeforeStop(this);

        // 更新状态，并在更新成功时触发AFTER_STOP事件
        if (!state.compareAndSet(NodeState.STOPPING, NodeState.TERMINATED)) {
            log.warn("Set JobTracker state to TERMINATED failed!");
        } else {
            triggerAfterStop(this);
        }

    }


    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public boolean isRunning() {
        return this.state.get() == NodeState.STARTED;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public boolean isStopped() {
        NodeState state = this.state.get();
        return state == NodeState.STOPPING || state == NodeState.TERMINATED;
    }

    @Override
    public JobTracker jobTracker() {
        if (NodeState.STARTED != this.state.get()) {
            throw new IllegalStateException("Job Tracker is not started!");
        }
        return jobTracker;
    }

    @Override
    public List<TrackerNodeDto> getTrackerNodes() {
        return trackerNodes;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public TrackerNodeLifecycle lifecycle() {
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @param worker worker节点
     * @return
     */
    @Override
    public Mono<Worker> registerWorker(Worker worker) {
        return workerManager.registerWorker(worker);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<Worker> availableWorkers() {
        return workerManager.availableWorkers();
    }

    /**
     * {@inheritDoc}
     *
     * @param workerId worker id。
     * @return
     */
    @Override
    public Mono<Worker> unregisterWorker(String workerId) {
        return workerManager.unregisterWorker(workerId);
    }

    /**
     * 节点状态
     */
    enum NodeState {

        INIT,

        STARTING,

        STARTED,

        STOPPING,

        TERMINATED

    }


    /**
     * {@inheritDoc}
     * 用于关闭JobTracker的实现。
     */
    class DisposableTrackerNodeBind implements DisposableTrackerNode {

        /**
         * {@inheritDoc}
         *
         * @return
         */
        @Override
        public TrackerNode node() {
            return ElectionTrackerNode.this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            ElectionTrackerNode node = ElectionTrackerNode.this;
            if (node.isStopped()) {
                return;
            }

            node.stop();
        }

        /**
         * {@inheritDoc}
         *
         * @return
         */
        @Override
        public boolean isDisposed() {
            return ElectionTrackerNode.this.isStopped();
        }
    }
}
