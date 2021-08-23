package org.limbo.flowjob.tracker.core.tracker.election;

import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.rpc.impl.BoltRaftRpcFactory;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.limbo.flowjob.tracker.commons.dto.tracker.TrackerNodeDto;
import org.limbo.flowjob.tracker.commons.utils.NetUtils;
import org.limbo.flowjob.tracker.core.raft.ElectionNode;
import org.limbo.flowjob.tracker.core.raft.ElectionNodeOptions;
import org.limbo.flowjob.tracker.core.raft.StateListener;
import org.limbo.flowjob.tracker.core.tracker.*;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.RpcCaller;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.processor.IsSchedulingProcessor;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.processor.NodeInfoProcessor;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.processor.ScheduleProcessor;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.processor.UnscheduleProcessor;
import org.limbo.flowjob.tracker.core.tracker.election.rpc.request.NodeInfoRequest;

import javax.annotation.PostConstruct;
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

    /**
     * 管理 worker
     */
    private WorkerManager workerManager;

    private JobTrackerFactory jobTrackerFactory;

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

    public ElectionTrackerNode(int port, ElectionNodeOptions electionOpts, JobTrackerFactory jobTrackerFactory,
                               WorkerManager workerManager) {
        this.jobTrackerFactory = jobTrackerFactory;
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
    @PostConstruct
    @Override
    public DisposableTrackerNode start() {
        // 重复启动检测
        if (!state.compareAndSet(NodeState.INIT, NodeState.STARTING)) {
            throw new IllegalStateException("JobTracker already running!");
        }

        // 生成DisposableJobTracker，触发BEFORE_START事件
        DisposableTrackerNodeBind disposable = new DisposableTrackerNodeBind(this);
        triggerBeforeStart(disposable);

        // 启动过程中被中断
        if (disposable.isDisposed()) {
            return disposable;
        }

        // 选举
        election(disposable);

        return disposable;
    }

    /**
     * 选举
     */
    private void election(DisposableTrackerNode disposable) {
        electionNode = new ElectionNode();
        registerProcessors(electionNode);
        electionNode.addStateListener(stateListener(disposable));
        electionNode.init(electionOpts);
    }

    private void registerProcessors(ElectionNode electionNode) {
        // 调度请求
        electionNode.addProcessor(new ScheduleProcessor(this));
        electionNode.addProcessor(new UnscheduleProcessor(this));
        electionNode.addProcessor(new IsSchedulingProcessor(this));
        // 节点信息
        electionNode.addProcessor(new NodeInfoProcessor(this));
    }

    private StateListener stateListener(DisposableTrackerNode disposable) {
        return new StateListener() {
            @Override
            public void onLeaderStart(long newTerm) {
                log.info("start Leader " + electionNode.getNode().getLeaderId());
                jobTracker = jobTrackerFactory.leader();

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
                jobTracker = jobTrackerFactory.follower(newLeaderId.getEndpoint(), rpcCaller);

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
        };
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

    public String getHost() {
        return null;
    }

    public int getPort() {
        return port;
    }

    @Override
    public List<TrackerNodeDto> getNodes() {
        // 向各个从节点发送指令 获取所有节点信息
        List<TrackerNodeDto> nodes = new ArrayList<>();

        // 自己的信息
        TrackerNodeDto self = new TrackerNodeDto();
        self.setHost(NetUtils.getLocalIp());
        self.setPort(port);
        nodes.add(self);

        // todo 1. 返回状态处理 2. 缓存 ？？？ 3. 不用发请求给自己了
        for (PeerId peerId : electionNode.getNode().listAlivePeers()) {
            ResponseDto<TrackerNodeDto> response = rpcCaller.invokeSync(peerId.getEndpoint(), new NodeInfoRequest());
            if (response.getCode() == 200 ) {
                nodes.add(response.getData());
            }
        }

        return nodes;
    }

    public ElectionNode getElectionNode() {
        return electionNode;
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

}
