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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.clent.dto.TaskReceiveDTO;
import org.limbo.flowjob.broker.api.constants.enums.TaskStatus;
import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.exceptions.JobDispatchException;
import org.limbo.flowjob.broker.core.plan.PlanInstance;
import org.limbo.flowjob.broker.core.plan.job.DispatchOption;
import org.limbo.flowjob.broker.core.plan.job.ExecutorOption;
import org.limbo.flowjob.broker.core.plan.job.JobInfo;
import org.limbo.flowjob.broker.core.plan.job.JobInstance;
import org.limbo.flowjob.broker.core.plan.job.dag.DAG;
import org.limbo.flowjob.broker.core.worker.Worker;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * 状态
     */
    private TaskStatus status;

    /**
     * 此分发执行此作业上下文的worker
     */
    private String workerId;

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

    /**
     * 重试次数
     */
    private Integer retry = 3;

    // -------- 非 po 属性

    /**
     * 作业分发配置参数
     */
    private DispatchOption dispatchOption;

    /**
     * 作业执行器配置参数
     */
    private ExecutorOption executorOption;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private transient WorkerManager workerManager;

    /**
     * 将此任务下发给worker。
     *
     * @param workerSelector 会将此上下文分发去执行的worker
     * @return 任务下发是否成功
     * @throws JobDispatchException 状态检测失败时，即此上下文的状态不是INIT或FAILED时抛出异常。
     */
    public boolean dispatch(WorkerSelector workerSelector) throws JobDispatchException {
        if (getStatus() != TaskStatus.DISPATCHING) {
            throw new JobDispatchException(jobId, taskId, "Cannot startup context due to current status: " + status);
        }

        List<Worker> availableWorkers = workerManager.availableWorkers();
        if (CollectionUtils.isEmpty(availableWorkers)) {
            return false;
        }
        for (int i = 0; i < retry; i++) {
            Worker worker = workerSelector.select(this, availableWorkers);
            if (worker == null) {
                return false;
            }

            boolean dispatched = doDispatch(worker);
            if (dispatched) {
                dispatched(worker);
                return true;
            }

            availableWorkers = availableWorkers.stream().filter(w -> !Objects.equals(w.getWorkerId(), worker.getWorkerId())).collect(Collectors.toList());
        }
        dispatchFailed();
        return false;
    }


    /**
     * 执行任务下发
     */
    protected boolean doDispatch(Worker worker) {
        try {
            // 发送任务到worker，根据worker返回结果，更新状态
            TaskReceiveDTO result = worker.sendTask(this);
            return result != null && result.getAccepted();
        } catch (Exception e) {
            throw new JobDispatchException(jobId, worker.getWorkerId(), "Context startup failed due to send job to worker error!", e);
        }
    }


    /**
     * worker确认接收此作业上下文，表示开始执行作业
     *
     * @param worker 确认接收此上下文的worker
     * @throws JobDispatchException 接受上下文的worker和上下文记录的worker不同时，抛出异常。
     */
    public void dispatched(Worker worker) throws JobDispatchException {
        // 不为此状态 无需更新
        if (this.getStatus() != TaskStatus.DISPATCHING) {
            return;
        }

        // 更新状态
        setStatus(TaskStatus.EXECUTING);
        setWorkerId(worker.getWorkerId());
    }


    /**
     * 任务下发失败
     *
     * @throws JobDispatchException 拒绝任务的worker和任务记录的worker不同时，抛出异常。
     */
    public void dispatchFailed() throws JobDispatchException {
        // 不为此状态 无需更新
        if (this.getStatus() != TaskStatus.DISPATCHING) {
            return;
        }

        // 更新状态
        setStatus(TaskStatus.DISPATCH_FAILED);
    }


    /**
     * 任务执行成功，worker反馈任务执行完成后，才会调用此方法。
     */
    public void succeed() {
        // 当前状态无需变更
        if (getStatus().isCompleted()) {
            return;
        }

        // 更新任务状态，更新失败说明已经处理过，CAS保证幂等
        setStatus(TaskStatus.SUCCEED);
    }


    /**
     * 下发后续任务
     *
     * @param planInstance 计划实例
     */
    protected void dispatchNextTask(PlanInstance planInstance) {

        // 从作业 DAG 中读取后续的作业节点
        DAG<JobInfo> dag = planInstance.getDag();
        List<JobInfo> subJobInfos = dag.subNodes(this.jobId);

        if (CollectionUtils.isEmpty(subJobInfos)) {

            // 后续作业不存在，需检测是否 Plan 执行完成
            if (planInstance.isAllJobFinished()) {
                planInstance.executeSucceed();
            }

        } else {

            // 后续作业存在，则检测是否可触发，并继续下发作业
            for (JobInfo subJobInfo : subJobInfos) {
                if (planInstance.isJobTriggerable(subJobInfo)) {
                    JobInstance subJobInstance = subJobInfo.newInstance(planInstance);
                    subJobInstance.dispatch();
                }
            }

        }
    }


    /**
     * 任务执行失败，worker反馈任务失败时，执行此方法。
     *
     * @param planInstance    执行计划实例
     * @param jobInstance     作业实例
     * @param errorMsg        执行失败的异常信息
     * @param errorStackTrace 执行失败的异常堆栈
     */
    public void failed(PlanInstance planInstance, JobInstance jobInstance, String errorMsg, String errorStackTrace) {
        // 当前状态无需变更
        if (getStatus().isCompleted()) {
            return;
        }

        // 更新任务状态，更新失败说明已经处理过，CAS保证幂等
        setStatus(TaskStatus.FAILED);
        setErrorMsg(errorMsg);
        setErrorStackTrace(errorStackTrace);
    }

}
