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

package org.limbo.flowjob.broker.core.plan.job.context;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.broker.api.clent.dto.TaskReceiveDTO;
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskResult;
import org.limbo.flowjob.broker.api.constants.enums.TaskScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.flowjob.broker.core.events.Event;
import org.limbo.flowjob.broker.core.events.EventTags;
import org.limbo.flowjob.broker.core.exceptions.JobDispatchException;
import org.limbo.flowjob.broker.core.plan.job.DispatchOption;
import org.limbo.flowjob.broker.core.plan.job.ExecutorOption;
import org.limbo.flowjob.broker.core.repositories.TaskRepository;
import org.limbo.flowjob.broker.core.worker.Worker;

import javax.inject.Inject;
import java.io.Serializable;
import java.time.Instant;

/**
 * 作业执行上下文
 *
 * @author Brozen
 * @since 2021-05-14
 */
@Getter
@Setter
@ToString
public class Task implements Serializable {
    private static final long serialVersionUID = -9164373359695671417L;

    private String taskId;

    private String planId;

    private String planInstanceId;

    private String jobId;

    private String jobInstanceId;

    /**
     * 调度状态
     */
    private TaskScheduleStatus state;

    /**
     * 执行结果
     */
    private TaskResult result;

    /**
     * 此分发执行此作业上下文的worker
     */
    private String workerId;

    /**
     * sharding normal
     */
    private TaskType type;

    /**
     * 作业属性，不可变。作业属性可用于分片作业、MapReduce作业、DAG工作流进行传参
     */
    private Attributes attributes;

    /**
     * 执行失败时的异常信息
     */
    private String errorMsg;

    /**
     * 执行失败时的异常堆栈
     */
    private String errorStackTrace;

    /**
     * 开始时间
     */
    private Instant startAt;

    /**
     * 结束时间
     */
    private Instant endAt;

    // -------- 非 po 属性

    /**
     * 作业分发配置参数
     */
    private DispatchOption dispatchOption;

    /**
     * 作业执行器配置参数
     */
    private ExecutorOption executorOption;

    // --------------------- 需注入
    @ToString.Exclude
    @Setter(onMethod_ = @Inject)
    private transient TaskRepository taskRepo;


    /**
     * 将此任务下发给worker。
     * 只有 {@link TaskScheduleStatus#SCHEDULING} 和 {@link TaskScheduleStatus#DISPATCH_FAILED} 状态的任务可被下发，代表首次下发和重试。
     *
     * @param worker 会将此上下文分发去执行的worker
     * @return 任务下发是否成功
     * @throws JobDispatchException 状态检测失败时，即此上下文的状态不是INIT或FAILED时抛出异常。
     */
    public boolean dispatch(Worker worker) throws JobDispatchException {
        // 检测状态
        TaskScheduleStatus status = getState();
        if (status != TaskScheduleStatus.SCHEDULING && status != TaskScheduleStatus.DISPATCH_FAILED) {
            throw new JobDispatchException(jobId, taskId, "Cannot startup context due to current status: " + status);
        }

        // 更新状态
        setState(TaskScheduleStatus.DISPATCHING);
        taskRepo.dispatching(this);

        // 下发任务
        return doDispatch(worker);
    }


    /**
     * 执行任务下发
     */
    private boolean doDispatch(Worker worker) {
        try {

            // 发送任务到worker，根据worker返回结果，更新状态
            TaskReceiveDTO result = worker.sendTask(this);
            if (result != null && result.getAccepted()) {

                this.accepted(worker);
                return true;

            } else {

                this.refused(worker);
                return false;

            }

        } catch (Exception e) {
            // 失败时更新上下文状态，冒泡异常
            // todo 如果是下发失败网络问题等，应该需要重试
//            setState(TaskScheduleStatus.FAILED);
            throw new JobDispatchException(jobId, worker.getWorkerId(),
                    "Context startup failed due to send job to worker error!", e);
        }
    }


    /**
     * worker确认接收此作业上下文，表示开始执行作业
     *
     * @param worker 确认接收此上下文的worker
     * @throws JobDispatchException 接受上下文的worker和上下文记录的worker不同时，抛出异常。
     */
    public void accepted(Worker worker) throws JobDispatchException {
        // 不为此状态 无需更新
        if (getState() != TaskScheduleStatus.DISPATCHING) {
            return;
        }

        // 更新状态
        setState(TaskScheduleStatus.EXECUTING);
        setWorkerId(worker.getWorkerId());
        taskRepo.dispatched(this);
    }


    /**
     * worker拒绝接收此任务，表示任务下发失败
     *
     * @param worker 拒绝接收的worker
     * @throws JobDispatchException 拒绝任务的worker和任务记录的worker不同时，抛出异常。
     */
    public void refused(Worker worker) throws JobDispatchException {
        // 不为此状态 无需更新
        if (getState() != TaskScheduleStatus.DISPATCHING) {
            return;
        }

        // 更新状态
        setState(TaskScheduleStatus.DISPATCH_FAILED);
        setWorkerId(worker.getWorkerId());
        taskRepo.dispatchFailed(this);
    }


    /**
     * 关闭上下文，绑定该上下文的作业成功执行完成后，才会调用此方法。
     *
     * FIXME 更新上下文，需锁定contextId，防止并发问题
     *
     * @throws JobDispatchException 上下文状态不是{@link JobScheduleStatus#EXECUTING}时抛出异常。
     */
    public void close() throws JobDispatchException {
        // 当前状态无需变更
        if (getState() == TaskScheduleStatus.COMPLETED) {
            return;
        }

        setState(TaskScheduleStatus.COMPLETED);
        setResult(TaskResult.SUCCEED);
//        setNeedPublish(true); todo

        // 发布领域事件
        Event<Task> acceptEvent = new Event<>(this);
        acceptEvent.setTag(EventTags.TASK_CLOSED);
//        eventPublisher.publish(acceptEvent); // todo
    }


    /**
     * 关闭上下文，绑定该上下文的作业执行失败后，调用此方法
     * @param errorMsg 执行失败的异常信息
     * @param errorStackTrace 执行失败的异常堆栈
     */
    public void close(String errorMsg, String errorStackTrace) {

        // 当前状态无需变更
        if (getState() == TaskScheduleStatus.COMPLETED) {
            return;
        }

        setState(TaskScheduleStatus.COMPLETED);
        setResult(TaskResult.SUCCEED);
        setErrorMsg(errorMsg);
        setErrorStackTrace(errorStackTrace);
//        setNeedPublish(true); // todo

        // 发布领域事件
        Event<Task> acceptEvent = new Event<>(this);
        acceptEvent.setTag(EventTags.TASK_CLOSED);
//        eventPublisher.publish(acceptEvent); // todo
    }

}
