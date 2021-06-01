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

package org.limbo.flowjob.tracker.core.job.context;

import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.tracker.commons.beans.domain.job.JobContext;
import org.limbo.flowjob.tracker.commons.constants.enums.JobContextStatus;
import org.limbo.flowjob.tracker.commons.exceptions.JobContextException;
import org.limbo.flowjob.tracker.commons.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.commons.beans.dto.SendJobResult;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerDO;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * 作业执行上下文
 *
 * @author Brozen
 * @since 2021-05-14
 */
public class JobContextDO extends JobContext {


    /**
     * 用于更新JobContext
     */
    private JobContextRepository jobContextRepository;

    /**
     * 用于触发、发布上下文生命周期事件
     */
    private FluxProcessor<JobContextLifecycleEvent, JobContextLifecycleEvent> lifecycleEventTrigger;

    public JobContextDO(JobContextRepository jobContextRepository) {
        this.jobContextRepository = Objects.requireNonNull(jobContextRepository, "JobContextRepository");
        this.lifecycleEventTrigger = DirectProcessor.create();
    }

    /**
     * 在指定worker上启动此作业上下文，将作业上下文发送给worker。
     *
     * FIXME 更新上下文，需锁定contextId，防止并发问题
     *
     * 只有{@link JobContextStatus#INIT}和{@link JobContextStatus#FAILED}状态的上下文可被开启。
     * @param worker 会将此上下文分发去执行的worker
     * @throws JobContextException 状态检测失败时，即此上下文的状态不是INIT或FAILED时抛出异常。
     */
    public void startupContext(WorkerDO worker) throws JobContextException {
        String jobId = getJobId();
        String contextId = getContextId();
        JobContextStatus status = getStatus();

        // 检测状态
        if (status != JobContextStatus.INIT && status != JobContextStatus.FAILED) {
            throw new JobContextException(jobId, contextId, "Cannot startup context due to current status: " + status);
        }

        // 更新上下文
        setWorkerId(worker.getId());
        setStatus(JobContextStatus.DISPATCHING);
        jobContextRepository.updateContext(this);

        try {

            // 发送上下文到worker
            Mono<SendJobResult> mono = worker.sendJobContext(this);
            // 发布事件
            lifecycleEventTrigger.onNext(JobContextLifecycleEvent.STARTED);

            // 等待发送结果，根据客户端接收结果，更新状态
            SendJobResult result = mono.block();
            if (result != null && result.getAccepted()) {
                this.acceptContext(worker);
            } else {
                this.refuseContext(worker);
            }

        } catch (JobWorkerException e) {
            // 失败时更新上下文状态，冒泡异常
            setStatus(JobContextStatus.FAILED);
            jobContextRepository.updateContext(this);

            throw new JobContextException(jobId, worker.getId(),
                    "Context startup failed due to send job to worker error!", e);
        }
    }

    /**
     * worker确认接收此作业上下文，表示开始执行作业
     *
     * FIXME 更新上下文，需锁定contextId，防止并发问题
     *
     * @param worker 确认接收此上下文的worker
     * @throws JobContextException 接受上下文的worker和上下文记录的worker不同时，抛出异常。
     */
    public void acceptContext(WorkerDO worker) throws JobContextException {

        assertContextStatus(JobContextStatus.DISPATCHING);
        assertWorkerId(worker.getId());

        // 更新状态
        setStatus(JobContextStatus.EXECUTING);
        jobContextRepository.updateContext(this);

        // 发布事件
        lifecycleEventTrigger.onNext(JobContextLifecycleEvent.ACCEPTED);
    }

    /**
     * worker拒绝接收此作业上下文，作业不会开始执行
     *
     * FIXME 更新上下文，需锁定contextId，防止并发问题
     *
     * @param worker 拒绝接收此上下文的worker
     * @throws JobContextException 拒绝上下文的worker和上下文记录的worker不同时，抛出异常。
     */
    public void refuseContext(WorkerDO worker) throws JobContextException {

        assertContextStatus(JobContextStatus.DISPATCHING);
        assertWorkerId(worker.getId());

        // 更新状态
        setStatus(JobContextStatus.REFUSED);
        jobContextRepository.updateContext(this);

        // 发布事件
        lifecycleEventTrigger.onNext(JobContextLifecycleEvent.REFUSED);
    }

    /**
     * 关闭上下文，只有绑定该上下文的作业执行完成后，才会调用此方法。
     *
     * FIXME 更新上下文，需锁定contextId，防止并发问题
     *
     * @throws JobContextException 上下文状态不是{@link JobContextStatus#EXECUTING}时抛出异常。
     */
    public void closeContext() throws JobContextException {

        assertContextStatus(JobContextStatus.EXECUTING);

        setStatus(JobContextStatus.SUCCEED);
        jobContextRepository.updateContext(this);

        // 发布事件
        lifecycleEventTrigger.onNext(JobContextLifecycleEvent.CLOSED);
        lifecycleEventTrigger.onComplete();
    }

    /**
     * 断言当前上下文处于某个状态，否则将抛出{@link JobContextException}
     * @param assertStatus 断言当前上下文的状态
     */
    protected void assertContextStatus(JobContextStatus assertStatus) throws JobContextException {
        if (getStatus() != assertStatus) {
            throw new JobContextException(getJobId(), getContextId(),
                    "Expect context status: " + assertStatus + " but is: " + getStatus());
        }
    }

    /**
     * 断言当前上下文的workerId是指定值，否则将抛出{@link JobContextException}
     * @param assertWorkerId 断言当前上下文的workerId
     */
    protected void assertWorkerId(String assertWorkerId) throws JobContextException {
        if (!StringUtils.equalsIgnoreCase(getWorkerId(), assertWorkerId)) {
            throw new JobContextException(getJobId(), getContextId(),
                    "Except worker: " + assertWorkerId + " but worker is: " + getWorkerId());
        }
    }

    /**
     * 上下文被worker拒绝时的回调监听。
     *
     * TODO 此方式只支持单机监听，如果tracker集群部署，监听需用其他方式处理
     *
     * @return
     */
    public Mono<JobContext> onContextRefused() {
        return Mono.create(sink -> this.lifecycleEventTrigger
                .filter(e -> e == JobContextLifecycleEvent.REFUSED)
                .subscribe(e -> sink.success(this), sink::error, sink::success));
    }

    /**
     * 上下文被worker接收时的回调监听。
     *
     * TODO 此方式只支持单机监听，如果tracker集群部署，监听需用其他方式处理
     *
     * @return
     */
    public Mono<JobContext> onContextAccepted() {
        return Mono.create(sink -> this.lifecycleEventTrigger
                .filter(e -> e == JobContextLifecycleEvent.ACCEPTED)
                .subscribe(e -> sink.success(this), sink::error, sink::success));
    }

    /**
     * 上下文被关闭时的回调监听。
     *
     * TODO 此方式只支持单机监听，如果tracker集群部署，监听需用其他方式处理
     *
     * @return
     */
    public Mono<JobContext> onContextClosed() {
        return Mono.create(sink -> this.lifecycleEventTrigger
                .filter(e -> e == JobContextLifecycleEvent.CLOSED)
                .subscribe(e -> sink.success(this), sink::error, sink::success));
    }

    /**
     * 上下文生命周期事件触发时的回调监听。
     *
     * TODO 此方式只支持单机监听，如果tracker集群部署，监听需用其他方式处理
     *
     * @return 声明周期事件发生时触发
     * @see JobContextLifecycleEvent
     */
    public Flux<JobContextLifecycleEvent> onLifecycleEvent() {
        return Flux.from(this.lifecycleEventTrigger);
    }

    /**
     * 上下文声明周期事件
     * <ul>
     *     <li><code>STARTED</code> - 上下文启动，正在分发给worker</li>
     *     <li><code>REFUSED</code> - worker拒绝接收上下文</li>
     *     <li><code>ACCEPTED</code> - worker成功接收上下文</li>
     *     <li><code>CLOSED</code> - 上下文被关闭</li>
     * </ul>
     */
    enum JobContextLifecycleEvent {

        /**
         * @see JobContextDO#startupContext(WorkerDO)
         */
        STARTED,

        /**
         * @see JobContextDO#refuseContext(WorkerDO)
         */
        REFUSED,

        /**
         * @see JobContextDO#acceptContext(WorkerDO)
         */
        ACCEPTED,

        /**
         * @see JobContextDO#closeContext()
         */
        CLOSED

    }

}
