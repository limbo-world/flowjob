package org.limbo.flowjob.broker.ha;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.api.constants.enums.BrokerNodeState;
import org.limbo.flowjob.broker.api.dto.broker.BrokerDTO;
import org.limbo.flowjob.broker.core.broker.DisposableTrackerNode;
import org.limbo.flowjob.broker.core.broker.DisposableTrackerNodeBind;
import org.limbo.flowjob.broker.core.broker.JobTracker;
import org.limbo.flowjob.broker.core.broker.ReactorTrackerNodeLifecycle;
import org.limbo.flowjob.broker.core.broker.TrackerNode;
import org.limbo.flowjob.broker.core.broker.TrackerNodeLifecycle;
import org.limbo.flowjob.broker.core.broker.WorkerManager;
import org.limbo.flowjob.broker.core.utils.NetUtils;

import javax.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicReference;

/**
 * todo 这个是不是抽象到core
 *
 * @author Devil
 * @since 2021/8/9
 */
@Slf4j
public abstract class AbstractTrackerNode extends ReactorTrackerNodeLifecycle implements TrackerNode {

    /**
     * 提供给worker的服务的 host
     */
    protected String host;

    /**
     * 提供给worker的服务port
     */
    protected int port;

    /**
     * 当前JobTracker状态
     */
    protected final AtomicReference<BrokerNodeState> state;

    /**
     * 管理 worker
     */
    protected WorkerManager workerManager;

    protected JobTrackerFactory jobTrackerFactory;

    /**
     * 当前的 jobTracker
     */
    protected JobTracker jobTracker;

    public AbstractTrackerNode(String host, int port, JobTrackerFactory jobTrackerFactory, WorkerManager workerManager) {
        this.host = host;
        if (StringUtils.isBlank(this.host)) {
            this.host = NetUtils.getLocalIp();
        }
        this.port = port;
        this.jobTrackerFactory = jobTrackerFactory;
        this.workerManager = workerManager;
        this.state = new AtomicReference<>(BrokerNodeState.INIT);

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
        if (!state.compareAndSet(BrokerNodeState.INIT, BrokerNodeState.STARTING)) {
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
        if (!state.compareAndSet(BrokerNodeState.STARTING, BrokerNodeState.STARTED)) {
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

        BrokerNodeState currState = state.get();
        if (currState != BrokerNodeState.STARTING && currState != BrokerNodeState.STARTED) {
            throw new IllegalStateException("Node is not running!");
        }

        if (state.compareAndSet(currState, BrokerNodeState.STOPPING)) {
            throw new IllegalStateException("Node is already stopped!");
        }

        // 触发BEFORE_STOP事件
        triggerBeforeStop(this);

        // 更新状态，并在更新成功时触发AFTER_STOP事件
        if (!state.compareAndSet(BrokerNodeState.STOPPING, BrokerNodeState.TERMINATED)) {
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
            throw new IllegalStateException("Node is not started!");
        }
        return jobTracker;
    }

    protected BrokerDTO getNodeInfo() {
        // 自己的信息
        BrokerDTO self = new BrokerDTO();
        self.setHost(host);
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
