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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.tracker.commons.constants.enums.DispatchType;
import org.limbo.flowjob.tracker.commons.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.tracker.commons.dto.worker.JobReceiveResult;
import org.limbo.flowjob.tracker.commons.exceptions.JobDispatchException;
import org.limbo.flowjob.tracker.commons.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;
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
public class JobInstance {

    /**
     * 计划ID
     */
    private String planId;

    /**
     * 计划的版本
     */
    private Integer version;

    /**
     * 计划实例的ID
     */
    private Integer planInstanceId;

    /**
     * 作业ID
     */
    private String jobId;

    /**
     * 从 1 开始增加 planId + version + planInstanceId + jobId + jobInstanceId 全局唯一
     */
    private Integer jobInstanceId;

    /**
     * 此上下文状态
     */
    private JobScheduleStatus state;

    /**
     * 下发方式
     */
    private DispatchType dispatchType;

    /**
     * 此分发执行此作业上下文的worker
     */
    private String workerId;

    /**
     * 此作业依赖的父节点ID
     */
    private List<String> parentJobIds;

    /**
     * 此作业完成后需要通知的子节点ID
     */
    private List<String> childrenJobIds;

    /**
     * 作业属性，不可变。作业属性可用于分片作业、MapReduce作业、DAG工作流进行传参
     */
    private JobAttributes jobAttributes;

    /**
     * 执行失败时的异常信息
     */
    private String errorMsg;

    /**
     * 执行失败时的异常堆栈
     */
    private String errorStackTrace;


    // ----------------------- 分隔
    /**
     * 用于更新JobContext
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private JobInstanceRepository jobInstanceRepository;

    /**
     * 用于触发、发布上下文生命周期事件
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private Sinks.Many<JobContextLifecycleEvent> lifecycleEventTrigger;

    public JobInstance(JobInstanceRepository jobInstanceRepository) {
        this.jobInstanceRepository = Objects.requireNonNull(jobInstanceRepository, "JobContextRepository");
        this.lifecycleEventTrigger = Sinks.many().multicast().directAllOrNothing();
    }

    /**
     * 在指定worker上启动此作业上下文，将作业上下文发送给worker。
     *
     * FIXME 更新上下文，需锁定contextId，防止并发问题
     *
     * 只有{@link JobScheduleStatus#Scheduling}和{@link JobScheduleStatus#FAILED}状态的上下文可被开启。
     * @param worker 会将此上下文分发去执行的worker
     * @throws JobDispatchException 状态检测失败时，即此上下文的状态不是INIT或FAILED时抛出异常。
     */
    public void startupContext(Worker worker) throws JobDispatchException {
        JobScheduleStatus status = getState();

        // 检测状态
        if (status != JobScheduleStatus.Scheduling && status != JobScheduleStatus.FAILED) {
            throw new JobDispatchException(getJobId(), getId(), "Cannot startup context due to current status: " + status);
        }

        try {

            // 发送上下文到worker
            Mono<JobReceiveResult> mono = worker.sendJobContext(this);
            // 发布事件
            lifecycleEventTrigger.emitNext(JobContextLifecycleEvent.STARTED, Sinks.EmitFailureHandler.FAIL_FAST);

            // 等待发送结果，根据客户端接收结果，更新状态
            JobReceiveResult result = mono.block();
            if (result != null && result.getAccepted()) {
                this.acceptContext(worker);
            } else {
                this.refuseContext(worker);
            }

        } catch (JobWorkerException e) {
            // 失败时更新上下文状态，冒泡异常
            setState(JobScheduleStatus.FAILED);
            jobInstanceRepository.updateInstance(this);

            throw new JobDispatchException(getJobId(), worker.getWorkerId(),
                    "Context startup failed due to send job to worker error!", e);
        }
    }

    /**
     * worker确认接收此作业上下文，表示开始执行作业
     *
     * FIXME 更新上下文，需锁定contextId，防止并发问题
     *
     * @param worker 确认接收此上下文的worker
     * @throws JobDispatchException 接受上下文的worker和上下文记录的worker不同时，抛出异常。
     */
    public void acceptContext(Worker worker) throws JobDispatchException {

        assertContextStatus(JobScheduleStatus.Scheduling);
        assertWorkerId(worker.getWorkerId());

        // 更新状态
        setState(JobScheduleStatus.EXECUTING);
        jobInstanceRepository.updateInstance(this);

        // 发布事件
        lifecycleEventTrigger.emitNext(JobContextLifecycleEvent.ACCEPTED, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * worker拒绝接收此作业上下文，作业不会开始执行
     *
     * FIXME 更新上下文，需锁定contextId，防止并发问题
     *
     * @param worker 拒绝接收此上下文的worker
     * @throws JobDispatchException 拒绝上下文的worker和上下文记录的worker不同时，抛出异常。
     */
    public void refuseContext(Worker worker) throws JobDispatchException {

        assertContextStatus(JobScheduleStatus.Scheduling);
        assertWorkerId(worker.getWorkerId());

        // 更新状态
//        setState(JobScheduleStatus.REFUSED);
//        jobInstanceRepository.updateInstance(this);

        // 发布事件
        lifecycleEventTrigger.emitNext(JobContextLifecycleEvent.REFUSED, Sinks.EmitFailureHandler.FAIL_FAST);
    }


    /**
     * 关闭上下文，绑定该上下文的作业成功执行完成后，才会调用此方法。
     *
     * FIXME 更新上下文，需锁定contextId，防止并发问题
     *
     * @throws JobDispatchException 上下文状态不是{@link JobScheduleStatus#EXECUTING}时抛出异常。
     */
    public void closeContext() throws JobDispatchException {

        assertContextStatus(JobScheduleStatus.EXECUTING);

        setState(JobScheduleStatus.SUCCEED);
        jobInstanceRepository.updateInstance(this);

        // 发布事件
        lifecycleEventTrigger.emitNext(JobContextLifecycleEvent.CLOSED, Sinks.EmitFailureHandler.FAIL_FAST);
        lifecycleEventTrigger.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
    }


    /**
     * 关闭上下文，绑定该上下文的作业执行失败后，调用此方法
     * @param errorMsg 执行失败的异常信息
     * @param errorStackTrace 执行失败的异常堆栈
     */
    public void closeContext(String errorMsg, String errorStackTrace) {

        assertContextStatus(JobScheduleStatus.EXECUTING);

        setState(JobScheduleStatus.FAILED);
        setErrorMsg(errorMsg);
        setErrorStackTrace(errorStackTrace);
        jobInstanceRepository.updateInstance(this);

        // 发布事件
        lifecycleEventTrigger.emitNext(JobContextLifecycleEvent.CLOSED, Sinks.EmitFailureHandler.FAIL_FAST);
        lifecycleEventTrigger.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
    }


    /**
     * 断言当前上下文处于某个状态，否则将抛出{@link JobDispatchException}
     * @param assertStatus 断言当前上下文的状态
     */
    protected void assertContextStatus(JobScheduleStatus assertStatus) throws JobDispatchException {
        if (getState() != assertStatus) {
            throw new JobDispatchException(getJobId(), getId(),
                    "Expect context status: " + assertStatus + " but is: " + getState());
        }
    }

    /**
     * 断言当前上下文的workerId是指定值，否则将抛出{@link JobDispatchException}
     * @param assertWorkerId 断言当前上下文的workerId
     */
    protected void assertWorkerId(String assertWorkerId) throws JobDispatchException {
        if (!StringUtils.equalsIgnoreCase(getWorkerId(), assertWorkerId)) {
            throw new JobDispatchException(getJobId(), getId(),
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
    public Mono<JobInstance> onContextRefused() {
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
    public Mono<JobInstance> onContextAccepted() {
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
    public Mono<JobInstance> onContextClosed() {
        return Mono.create(sink -> this.lifecycleEventTrigger
                .asFlux()
                .filter(e -> e == JobContextLifecycleEvent.CLOSED)
                .subscribe(e -> sink.success(this), sink::error, sink::success));
    }

    /**
     * 获取全局唯一的实例ID
     * @return id
     */
    public String getId() {
        return planId + "-" + version + "-" + planInstanceId + "-" + jobId + "-" + jobInstanceId;
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
         * @see JobInstance#startupContext(Worker)
         */
        STARTED,

        /**
         * @see JobInstance#refuseContext(Worker)
         */
        REFUSED,

        /**
         * @see JobInstance#acceptContext(Worker)
         */
        ACCEPTED,

        /**
         * @see JobInstance#closeContext()
         */
        CLOSED

    }

}
