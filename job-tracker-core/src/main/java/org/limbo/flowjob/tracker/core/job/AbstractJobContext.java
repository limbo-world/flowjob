package org.limbo.flowjob.tracker.core.job;

import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.tracker.core.exceptions.JobContextException;
import org.limbo.flowjob.tracker.core.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * 作业上下文生命周期
 *
 * @author Brozen
 * @since 2021-05-21
 */
public abstract class AbstractJobContext implements JobContext {

    /**
     * 用于更新JobContext
     */
    private JobContextRepository jobContextRepository;

    /**
     * 用于触发、发布上下文生命周期事件
     */
    private FluxProcessor<JobContextLifecycleEvent, JobContextLifecycleEvent> lifecycleEventTrigger;

    public AbstractJobContext(JobContextRepository jobContextRepository) {
        this.jobContextRepository = Objects.requireNonNull(jobContextRepository, "JobContextRepository");
        this.lifecycleEventTrigger = DirectProcessor.create();
    }

    /**
     * 设置当前上下文的状态
     * @param status 上下文状态
     */
    protected abstract void setStatus(Status status);

    /**
     * 设置当前上下文分配的worker ID
     * @param workerId worker的ID
     */
    protected abstract void setWorkerId(String workerId);


    /**
     * {@inheritDoc}
     * @param worker 会将此上下文分发去执行的worker
     * @throws JobContextException 状态检测失败时，即此上下文的状态不是INIT或FAILED时抛出异常。
     */
    @Override
    public void startupContext(Worker worker) throws JobContextException {
        String jobId = getJobId();
        String contextId = getContextId();
        Status status = getStatus();

        // 检测状态
        if (status != JobContextStatus.INIT && status != JobContextStatus.FAILED) {
            throw new JobContextException(jobId, contextId, "Cannot startup context due to current status: " + status);
        }

        // FIXME 更新上下文，需锁定contextId，防止并发问题
        setWorkerId(worker.getId());
        setStatus(JobContextStatus.DISPATCHING);
        jobContextRepository.updateContext(this);

        try {

            // 发送上下文到worker
            worker.sendJobContext(this);
            // 发布事件
            lifecycleEventTrigger.onNext(JobContextLifecycleEvent.STARTED);

        } catch (JobWorkerException e) {
            // 失败时更新上下文状态，冒泡异常
            setStatus(JobContextStatus.FAILED);
            jobContextRepository.updateContext(this);

            throw new JobContextException(jobId, worker.getId(),
                    "Context startup failed due to send job to worker error!", e);
        }
    }

    /**
     * {@inheritDoc}
     * @param worker 确认接收此上下文的worker
     * @throws JobContextException 接受上下文的worker和上下文记录的worker不同时，抛出异常。
     */
    @Override
    public void acceptContext(Worker worker) throws JobContextException {

        assertContextStatus(JobContextStatus.DISPATCHING);
        assertWorkerId(worker.getId());

        // 更新状态
        // FIXME 更新上下文，需锁定contextId，防止并发问题
        setStatus(JobContextStatus.EXECUTING);
        jobContextRepository.updateContext(this);

        // 发布事件
        lifecycleEventTrigger.onNext(JobContextLifecycleEvent.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     * @param worker 拒绝接收此上下文的worker
     * @throws JobContextException 拒绝上下文的worker和上下文记录的worker不同时，抛出异常。
     */
    @Override
    public void refuseContext(Worker worker) throws JobContextException {

        assertContextStatus(JobContextStatus.DISPATCHING);
        assertWorkerId(worker.getId());

        // 更新状态
        // FIXME 更新上下文，需锁定contextId，防止并发问题
        setStatus(JobContextStatus.REFUSED);
        jobContextRepository.updateContext(this);

        // 发布事件
        lifecycleEventTrigger.onNext(JobContextLifecycleEvent.REFUSED);
    }

    /**
     * {@inheritDoc}
     * @throws JobContextException 上下文状态不是{@link JobContextStatus#EXECUTING}时抛出异常。
     */
    @Override
    public void closeContext() throws JobContextException {

        assertContextStatus(JobContextStatus.EXECUTING);

        // FIXME 更新上下文，需锁定contextId，防止并发问题
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
    protected void assertContextStatus(JobContext.Status assertStatus) throws JobContextException {
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
     * {@inheritDoc}
     * @return
     */
    @Override
    public Mono<JobContext> onContextRefused() {
        return Mono.create(sink -> this.lifecycleEventTrigger
                .filter(e -> e == JobContextLifecycleEvent.REFUSED)
                .subscribe(e -> sink.success(this), sink::error, sink::success));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Mono<JobContext> onContextAccepted() {
        return Mono.create(sink -> this.lifecycleEventTrigger
                .filter(e -> e == JobContextLifecycleEvent.ACCEPTED)
                .subscribe(e -> sink.success(this), sink::error, sink::success));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Mono<JobContext> onContextClosed() {
        return Mono.create(sink -> this.lifecycleEventTrigger
                .filter(e -> e == JobContextLifecycleEvent.CLOSED)
                .subscribe(e -> sink.success(this), sink::error, sink::success));
    }

    /**
     * 上下文生命周期事件触发时的回调监听。
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
         * @see AbstractJobContext#startupContext(Worker)
         */
        STARTED,

        /**
         * @see AbstractJobContext#refuseContext(Worker)
         */
        REFUSED,

        /**
         * @see AbstractJobContext#acceptContext(Worker)
         */
        ACCEPTED,

        /**
         * @see AbstractJobContext#closeContext()
         */
        CLOSED

    }

}
