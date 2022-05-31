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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.limbo.flowjob.broker.api.constants.enums.JobScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskResult;
import org.limbo.flowjob.broker.api.constants.enums.TaskScheduleStatus;
import org.limbo.flowjob.broker.api.constants.enums.TaskType;
import org.limbo.flowjob.tracker.commons.dto.worker.JobReceiveResult;
import org.limbo.flowjob.tracker.commons.exceptions.JobDispatchException;
import org.limbo.flowjob.tracker.core.evnets.Event;
import org.limbo.flowjob.tracker.core.evnets.EventPublisher;
import org.limbo.flowjob.tracker.core.evnets.EventTags;
import org.limbo.flowjob.tracker.core.job.DispatchOption;
import org.limbo.flowjob.tracker.core.job.ExecutorOption;
import org.limbo.flowjob.tracker.core.plan.PlanInstance;
import org.limbo.flowjob.tracker.core.plan.PlanRecord;
import org.limbo.flowjob.tracker.core.tracker.worker.Worker;
import reactor.core.publisher.Mono;

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

    /**
     * 多字段联合ID
     */
    private ID id;

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

    /**
     * todo 由于部分情况如当前task状态校验失败等，此task则无需后续执行 比如同个task下发多次
     */
    private boolean needPublish;

    /**
     * 事件发布器
     */
    @Inject
    private EventPublisher<Event<?>> eventPublisher;



    /**
     * 在指定worker上启动此作业上下文，将作业上下文发送给worker。
     *
     * FIXME 更新上下文，需锁定contextId，防止并发问题
     *
     * 只有{@link JobScheduleStatus#SCHEDULING}和{@link JobScheduleStatus#FAILED}状态的上下文可被开启。
     * @param worker 会将此上下文分发去执行的worker
     * @throws JobDispatchException 状态检测失败时，即此上下文的状态不是INIT或FAILED时抛出异常。
     */
    public void startup(Worker worker) throws JobDispatchException {
        TaskScheduleStatus status = getState();

        // 检测状态
        if (status != TaskScheduleStatus.SCHEDULING) {
            throw new JobDispatchException(getId().jobId, getId().taskId, "Cannot startup context due to current status: " + status);
        }

        try {
            // 发送上下文到worker
            Mono<JobReceiveResult> mono = worker.sendTask(this);

            // 等待发送结果，根据客户端接收结果，更新状态
            JobReceiveResult result = mono.block();
            if (result != null && result.getAccepted()) {
                this.accept(worker);
            } else {
                this.refuse(worker);
            }
        } catch (Exception e) {
            // 失败时更新上下文状态，冒泡异常
            // todo 如果是下发失败网络问题等，应该需要重试
//            setState(TaskScheduleStatus.FAILED);
            throw new JobDispatchException(getId().jobId, worker.getWorkerId(),
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
    public void accept(Worker worker) throws JobDispatchException {
        // 不为此状态 无需更新
        if (getState() != TaskScheduleStatus.SCHEDULING) {
            return;
        }

        // 更新状态
        setState(TaskScheduleStatus.EXECUTING);
        setWorkerId(worker.getWorkerId());
        setNeedPublish(true);

        // 发布领域事件
        Event<Task> acceptEvent = new Event<>(this);
        acceptEvent.setTag(EventTags.TASK_ACCEPTED);
        eventPublisher.publish(acceptEvent);
    }

    /**
     * worker拒绝接收此作业上下文，作业不会开始执行
     *
     * FIXME 更新上下文，需锁定contextId，防止并发问题
     *
     * @param worker 拒绝接收此上下文的worker
     * @throws JobDispatchException 拒绝上下文的worker和上下文记录的worker不同时，抛出异常。
     */
    public void refuse(Worker worker) throws JobDispatchException {
        // 不为此状态 无需更新
        if (getState() != TaskScheduleStatus.SCHEDULING) {
            return;
        }

        // todo 拒绝了 应该把task丢到队列尾部 等待重试

        // 发布领域事件
        Event<Task> acceptEvent = new Event<>(this);
        acceptEvent.setTag(EventTags.TASK_REFUSED);
        eventPublisher.publish(acceptEvent);
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
        setNeedPublish(true);

        // 发布领域事件
        Event<Task> acceptEvent = new Event<>(this);
        acceptEvent.setTag(EventTags.TASK_CLOSED);
        eventPublisher.publish(acceptEvent);
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
        setNeedPublish(true);

        // 发布领域事件
        Event<Task> acceptEvent = new Event<>(this);
        acceptEvent.setTag(EventTags.TASK_CLOSED);
        eventPublisher.publish(acceptEvent);
    }



    /**
     * 值对象，多字段联合ID
     */
    public static class ID {

        public final String planId;

        public final Long planRecordId;

        public final Integer planInstanceId;

        public final String jobId;

        public final Integer jobInstanceId;

        public final String taskId;

        public ID(String planId, Long planRecordId, Integer planInstanceId, String jobId, Integer jobInstanceId, String taskId) {
            this.planId = planId;
            this.planRecordId = planRecordId;
            this.planInstanceId = planInstanceId;
            this.jobId = jobId;
            this.jobInstanceId = jobInstanceId;
            this.taskId = taskId;
        }



        /**
         * 获取任务对应的 JobRecord.ID
         */
        public JobRecord.ID idOfJobRecord() {
            return new JobRecord.ID(planId, planRecordId, planInstanceId, jobId);
        }


        /**
         * 获取任务对应的 JobInstance.ID
         */
        public JobInstance.ID idOfJobInstance() {
            return new JobInstance.ID(planId, planRecordId, planInstanceId, jobId, jobInstanceId);
        }


        /**
         * 获取任务对应的 PlanInstance.ID
         */
        public PlanInstance.ID idOfPlanInstance() {
            return new PlanInstance.ID(planId, planRecordId, planInstanceId);
        }


        /**
         * 获取任务对应的 PlanRecord.ID
         */
        public PlanRecord.ID idOfPlanRecord() {
            return new PlanRecord.ID(planId, planRecordId);
        }
    }

}
