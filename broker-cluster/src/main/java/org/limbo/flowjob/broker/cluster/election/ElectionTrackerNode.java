package org.limbo.flowjob.broker.cluster.election;

import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.rpc.impl.BoltRaftRpcFactory;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.broker.api.constants.enums.BrokerNodeState;
import org.limbo.flowjob.broker.api.dto.BrokerDTO;
import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.broker.cluster.JobTrackerFactory;
import org.limbo.flowjob.broker.cluster.election.rpc.RpcCaller;
import org.limbo.flowjob.broker.cluster.election.rpc.processor.IsSchedulingProcessor;
import org.limbo.flowjob.broker.cluster.election.rpc.processor.NodeInfoProcessor;
import org.limbo.flowjob.broker.cluster.election.rpc.processor.ScheduleProcessor;
import org.limbo.flowjob.broker.cluster.election.rpc.processor.UnscheduleProcessor;
import org.limbo.flowjob.broker.cluster.election.rpc.request.NodeInfoRequest;
import org.limbo.flowjob.broker.core.broker.DisposableTrackerNode;
import org.limbo.flowjob.broker.core.broker.DisposableTrackerNodeBind;
import org.limbo.flowjob.broker.core.broker.JobTracker;
import org.limbo.flowjob.broker.core.broker.ReactorTrackerNodeLifecycle;
import org.limbo.flowjob.broker.core.broker.TrackerNode;
import org.limbo.flowjob.broker.core.broker.TrackerNodeLifecycle;
import org.limbo.flowjob.broker.core.broker.WorkerManager;
import org.limbo.flowjob.broker.core.utils.NetUtils;

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
    private final AtomicReference<BrokerNodeState> state;

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
        this.state = new AtomicReference<>(BrokerNodeState.INIT);
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
        if (!state.compareAndSet(BrokerNodeState.INIT, BrokerNodeState.STARTING)) {
            throw new IllegalStateException("Tracker already running!");
        }

        // 生成DisposableJobTracker，触发BEFORE_START事件
        // FIXME 事件需要同步触发，才能保证事件处理者调用dispose后，下面的判断可以检测到
        //       所以这里triggerBeforeStart用的Flux异步处理就不太合适了，需要改下
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
                if (state.compareAndSet(BrokerNodeState.STARTING, BrokerNodeState.STARTED)) {
                    triggerAfterStart(disposable);
                }
            }

            // todo 状态变更，是不是要变更为选举中，在这段时间内停止响应
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
                if (state.compareAndSet(BrokerNodeState.STARTING, BrokerNodeState.STARTED)) {
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

        BrokerNodeState currState = state.get();
        if (currState != BrokerNodeState.STARTING && currState != BrokerNodeState.STARTED) {
            throw new IllegalStateException("JobTracker is not running!");
        }

        if (state.compareAndSet(currState, BrokerNodeState.STOPPING)) {
            throw new IllegalStateException("JobTracker already stopped!");
        }

        // 触发BEFORE_STOP事件
        triggerBeforeStop(this);

        // 更新状态，并在更新成功时触发AFTER_STOP事件
        if (!state.compareAndSet(BrokerNodeState.STOPPING, BrokerNodeState.TERMINATED)) {
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
        return this.state.get() == BrokerNodeState.STARTED;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public boolean isStopped() {
        BrokerNodeState state = this.state.get();
        return state == BrokerNodeState.STOPPING || state == BrokerNodeState.TERMINATED;
    }

    @Override
    public JobTracker jobTracker() {
        if (BrokerNodeState.STARTED != this.state.get()) {
            throw new IllegalStateException("Job Tracker is not started!");
        }
        // FIXME 如果丢失了主节点身份，JobTracker为null，是否需要阻塞一下？
        return jobTracker;
    }

    public String getHost() {
        return null;
    }

    public int getPort() {
        return port;
    }

    @Override
    public List<BrokerDTO> getNodes() {
        // 向各个从节点发送指令 获取所有节点信息
        List<BrokerDTO> nodes = new ArrayList<>();

        // 自己的信息
        BrokerDTO self = new BrokerDTO();
        self.setHost(NetUtils.getLocalIp());
        self.setPort(port);
        nodes.add(self);

        // todo 1. 返回状态处理 2. 缓存 ？？？ 3. 不用发请求给自己了
        for (PeerId peerId : electionNode.getNode().listAlivePeers()) {
            ResponseDTO<BrokerDTO> response = rpcCaller.invokeSync(peerId.getEndpoint(), new NodeInfoRequest());
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
