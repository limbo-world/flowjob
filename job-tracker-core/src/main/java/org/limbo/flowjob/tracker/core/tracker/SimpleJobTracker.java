package org.limbo.flowjob.tracker.core.tracker;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 基于
 *
 * @author Brozen
 * @since 2021-06-01
 */
@Slf4j
public class SimpleJobTracker extends AbstractJobTracker {

    /**
     * 当前JobTracker状态
     */
    protected AtomicReference<JobTrackerState> state;

    public SimpleJobTracker(WorkerRepository workerRepository) {
        super(workerRepository);
        state = new AtomicReference<>(JobTrackerState.INIT);
    }

    /**
     * {@inheritDoc}<br/>
     * 这里也许会要进行一些资源初始化？
     *
     * @return
     */
    @PostConstruct
    @Override
    public DisposableJobTracker start() {
        // 重复启动检测
        if (!state.compareAndSet(JobTrackerState.INIT, JobTrackerState.STARTING)) {
            throw new IllegalStateException("JobTracker already running!");
        }

        // 生成DisposableJobTracker，触发BEFORE_START事件
        DisposableJobTrackerBind disposable = new DisposableJobTrackerBind();
        triggerBeforeStart(disposable);

        // 启动过程中被中断
        if (disposable.isDisposed()) {
            return disposable;
        }

        // 设置状态，成功则触发AFTER_START事件
        if (!state.compareAndSet(JobTrackerState.STARTING, JobTrackerState.STARTED)) {
            log.warn("Set JobTracker state to STARTED failed, maybe stop() is called in a async thread?");
        } else {
            triggerAfterStart(disposable);
        }

        return disposable;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @PreDestroy
    @Override
    public void stop() {

        JobTrackerState currState = state.get();
        if (currState != JobTrackerState.STARTING && currState != JobTrackerState.STARTED) {
            throw new IllegalStateException("JobTracker is not running!");
        }

        if (state.compareAndSet(currState, JobTrackerState.STOPPING)) {
            throw new IllegalStateException("JobTracker already stopped!");
        }

        // 触发BEFORE_STOP事件
        triggerBeforeStop(this);

        // 更新状态，并在更新成功时触发AFTER_STOP事件
        if (!state.compareAndSet(JobTrackerState.STOPPING, JobTrackerState.TERMINATED)) {
            log.warn("Set JobTracker state to TERMINATED failed!");
        } else {
            triggerAfterStop(this);
        }

    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public boolean isRunning() {
        return this.state.get() == JobTrackerState.STARTED;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public boolean isStopped() {
        JobTrackerState state = this.state.get();
        return state == JobTrackerState.STOPPING || state == JobTrackerState.TERMINATED;
    }

    /**
     * JobTracker的状态
     */
    enum JobTrackerState {

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
    class DisposableJobTrackerBind implements DisposableJobTracker {

        /**
         * {@inheritDoc}
         * @return
         */
        @Override
        public JobTracker jobTracker() {
            return SimpleJobTracker.this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            SimpleJobTracker jobTracker = SimpleJobTracker.this;
            if (jobTracker.isStopped()) {
                return;
            }

            jobTracker.stop();
        }

        /**
         * {@inheritDoc}
         * @return
         */
        @Override
        public boolean isDisposed() {
            return SimpleJobTracker.this.isStopped();
        }
    }

}
