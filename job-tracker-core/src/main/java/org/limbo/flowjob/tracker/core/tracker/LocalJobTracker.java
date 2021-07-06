/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.tracker.core.tracker;

import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 此类型所表示的JobTracker运行在当前程序所在的JVM中，表示本地JobTracker，调用此JobTracker方法时，逻辑在调用方所在的JVM中执行。
 *
 * @author Brozen
 * @since 2021-06-01
 */
@Slf4j
public abstract class LocalJobTracker extends ReactorJobTrackerLifecycle implements JobTracker {

    /**
     * 当前JobTracker状态
     */
    protected AtomicReference<LeaderJobTracker.JobTrackerState> state;

    /**
     * 用户管理worker，实现WorkerManager的相关功能
     */
    private WorkerRepository workerRepository;

    public LocalJobTracker(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
        this.state = new AtomicReference<>(LeaderJobTracker.JobTrackerState.INIT);
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
        if (!state.compareAndSet(LeaderJobTracker.JobTrackerState.INIT, LeaderJobTracker.JobTrackerState.STARTING)) {
            throw new IllegalStateException("JobTracker already running!");
        }

        // 生成DisposableJobTracker，触发BEFORE_START事件
        LeaderJobTracker.DisposableJobTrackerBind disposable = new LeaderJobTracker.DisposableJobTrackerBind();
        triggerBeforeStart(disposable);

        // 启动过程中被中断
        if (disposable.isDisposed()) {
            return disposable;
        }

        // 设置状态，成功则触发AFTER_START事件
        if (!state.compareAndSet(LeaderJobTracker.JobTrackerState.STARTING, LeaderJobTracker.JobTrackerState.STARTED)) {
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

        LeaderJobTracker.JobTrackerState currState = state.get();
        if (currState != LeaderJobTracker.JobTrackerState.STARTING && currState != LeaderJobTracker.JobTrackerState.STARTED) {
            throw new IllegalStateException("JobTracker is not running!");
        }

        if (state.compareAndSet(currState, LeaderJobTracker.JobTrackerState.STOPPING)) {
            throw new IllegalStateException("JobTracker already stopped!");
        }

        // 触发BEFORE_STOP事件
        triggerBeforeStop(this);

        // 更新状态，并在更新成功时触发AFTER_STOP事件
        if (!state.compareAndSet(LeaderJobTracker.JobTrackerState.STOPPING, LeaderJobTracker.JobTrackerState.TERMINATED)) {
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
        return this.state.get() == LeaderJobTracker.JobTrackerState.STARTED;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public boolean isStopped() {
        LeaderJobTracker.JobTrackerState state = this.state.get();
        return state == LeaderJobTracker.JobTrackerState.STOPPING || state == LeaderJobTracker.JobTrackerState.TERMINATED;
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
            return LocalJobTracker.this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            LocalJobTracker jobTracker = LocalJobTracker.this;
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
            return LocalJobTracker.this.isStopped();
        }
    }

}
