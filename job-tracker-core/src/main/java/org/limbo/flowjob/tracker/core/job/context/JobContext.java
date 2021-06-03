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

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.JobContextStatus;
import org.limbo.flowjob.tracker.commons.dto.worker.SendJobResult;
import org.limbo.flowjob.tracker.commons.exceptions.JobContextException;
import org.limbo.flowjob.tracker.commons.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import reactor.core.publisher.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 作业执行上下文
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Getter
@Setter
@ToString
public class JobContext {

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * 执行上下文ID。一个作业可能在调度中，有两次同时在执行，因此可能会产生两个context，需要用contextId做区分。
     */
    private String contextId;

    /**
     * 此上下文状态
     */
    private JobContextStatus status;

    /**
     * 此分发执行此作业上下文的worker
     */
    private String workerId;

    /**
     * 此上下文的创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 此上下文的更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 作业属性，不可变。作业属性可用于分片作业、MapReduce作业、DAG工作流进行传参
     */
    private JobAttributes jobAttributes;


    // ----------------------- 分隔
    /**
     * 用于更新JobContext
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private JobContextRepository jobContextRepository;

    /**
     * 用于触发、发布上下文生命周期事件
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private Sinks.Many<JobContextLifecycleEvent> lifecycleEventTrigger;

    public JobContext(JobContextRepository jobContextRepository) {
        this.jobContextRepository = Objects.requireNonNull(jobContextRepository, "JobContextRepository");
        this.lifecycleEventTrigger = Sinks.many().multicast().directAllOrNothing();
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
    public void startupContext(Worker worker) throws JobContextException {
        String jobId = getJobId();
        String contextId = getContextId();
        JobContextStatus status = getStatus();

        // 检测状态
        if (status != JobContextStatus.INIT && status != JobContextStatus.FAILED) {
            throw new JobContextException(jobId, contextId, "Cannot startup context due to current status: " + status);
        }

        // 更新上下文
        setWorkerId(worker.getWorkerId());
        setStatus(JobContextStatus.DISPATCHING);
        jobContextRepository.updateContext(this);

        try {

            // 发送上下文到worker
            Mono<SendJobResult> mono = worker.sendJobContext(this);
            // 发布事件
            lifecycleEventTrigger.emitNext(JobContextLifecycleEvent.STARTED, Sinks.EmitFailureHandler.FAIL_FAST);

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

            throw new JobContextException(jobId, worker.getWorkerId(),
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
    public void acceptContext(Worker worker) throws JobContextException {

        assertContextStatus(JobContextStatus.DISPATCHING);
        assertWorkerId(worker.getWorkerId());

        // 更新状态
        setStatus(JobContextStatus.EXECUTING);
        jobContextRepository.updateContext(this);

        // 发布事件
        lifecycleEventTrigger.emitNext(JobContextLifecycleEvent.ACCEPTED, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * worker拒绝接收此作业上下文，作业不会开始执行
     *
     * FIXME 更新上下文，需锁定contextId，防止并发问题
     *
     * @param worker 拒绝接收此上下文的worker
     * @throws JobContextException 拒绝上下文的worker和上下文记录的worker不同时，抛出异常。
     */
    public void refuseContext(Worker worker) throws JobContextException {

        assertContextStatus(JobContextStatus.DISPATCHING);
        assertWorkerId(worker.getWorkerId());

        // 更新状态
        setStatus(JobContextStatus.REFUSED);
        jobContextRepository.updateContext(this);

        // 发布事件
        lifecycleEventTrigger.emitNext(JobContextLifecycleEvent.REFUSED, Sinks.EmitFailureHandler.FAIL_FAST);
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
        lifecycleEventTrigger.emitNext(JobContextLifecycleEvent.CLOSED, Sinks.EmitFailureHandler.FAIL_FAST);
        lifecycleEventTrigger.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
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
                .asFlux()
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
                .asFlux()
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
                .asFlux()
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
        return this.lifecycleEventTrigger.asFlux();
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
         * @see JobContext#startupContext(Worker)
         */
        STARTED,

        /**
         * @see JobContext#refuseContext(Worker)
         */
        REFUSED,

        /**
         * @see JobContext#acceptContext(Worker)
         */
        ACCEPTED,

        /**
         * @see JobContext#closeContext()
         */
        CLOSED

    }

}
