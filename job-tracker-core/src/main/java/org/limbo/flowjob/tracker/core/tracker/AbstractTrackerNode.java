package org.limbo.flowjob.tracker.core.tracker;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.tracker.commons.dto.tracker.TrackerNodeDto;
import org.limbo.flowjob.tracker.commons.utils.NetUtils;

import javax.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Devil
 * @since 2021/8/9
 */
@Slf4j
public abstract class AbstractTrackerNode extends ReactorTrackerNodeLifecycle implements TrackerNode {

    /**
     * 提供给worker的服务的hostname
     */
    protected String hostname;

    /**
     * 提供给worker的服务port
     */
    protected int port;
    /**
     * 当前JobTracker状态
     */
    protected final AtomicReference<NodeState> state;

    /**
     * 管理 worker
     */
    protected WorkerManager workerManager;

    protected JobTrackerFactory jobTrackerFactory;

    /**
     * 当前的 jobTracker
     */
    protected JobTracker jobTracker;

    public AbstractTrackerNode(String hostname, int port, JobTrackerFactory jobTrackerFactory, WorkerManager workerManager) {
        this.hostname = hostname;
        if (StringUtils.isBlank(this.hostname)) {
            this.hostname = NetUtils.getLocalIp();
        }
        this.port = port;
        this.jobTrackerFactory = jobTrackerFactory;
        this.workerManager = workerManager;
        this.state = new AtomicReference<>(NodeState.INIT);

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
            throw new IllegalStateException("Node is already running!");
        }

        // 生成DisposableJobTracker，触发BEFORE_START事件
        DisposableTrackerNodeBind disposable = new DisposableTrackerNodeBind(this);

        // 启动前执行
        triggerBeforeStart(disposable);

        // 启动过程中被中断
        if (disposable.isDisposed()) {
            return disposable;
        }

        // 设置状态，成功则触发AFTER_START事件
        if (!state.compareAndSet(NodeState.STARTING, NodeState.STARTED)) {
            throw new IllegalStateException("Set Node state to STARTED failed, maybe stop() is called in a async thread?");
        }

        // 启动后执行
        triggerAfterStart(disposable);

        return disposable;
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
            throw new IllegalStateException("Node is not running!");
        }

        if (state.compareAndSet(currState, NodeState.STOPPING)) {
            throw new IllegalStateException("Node is already stopped!");
        }

        // 触发BEFORE_STOP事件
        triggerBeforeStop(this);

        // 更新状态，并在更新成功时触发AFTER_STOP事件
        if (!state.compareAndSet(NodeState.STOPPING, NodeState.TERMINATED)) {
            log.warn("Set Node state to TERMINATED failed!");
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
            throw new IllegalStateException("Node is not started!");
        }
        return jobTracker;
    }

    protected TrackerNodeDto getNodeInfo() {
        // 自己的信息
        TrackerNodeDto self = new TrackerNodeDto();
        self.setHostname(hostname);
        self.setPort(port);
        return self;
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
