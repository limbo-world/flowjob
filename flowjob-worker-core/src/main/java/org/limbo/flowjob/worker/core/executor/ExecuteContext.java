/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.core.executor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.common.constants.TaskConstant;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.rpc.WorkerAgentRpc;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class ExecuteContext implements Runnable {

    enum Status {
        WAITING, RUNNING, SUCCEED, FAILED, CANCELED
    }

    private ScheduledExecutorService scheduledReportPool;

    public final TaskRepository taskRepository;

    /**
     * 任务执行器
     */
    public final TaskExecutor executor;

    /**
     * Rpc
     */
    @Getter
    public final WorkerAgentRpc agentRpc;

    /**
     * 从 Broker 接收到的任务
     */
    @Getter
    public final Task task;

    /**
     * 当前任务被调度的 Future
     */
    @Setter
    private Future<?> scheduleFuture;

    /**
     * 任务执行状态
     */
    private final AtomicReference<Status> status;

    private ScheduledFuture<?> taskReportScheduledFuture;

    public ExecuteContext(ScheduledExecutorService scheduledReportPool, TaskRepository taskRepository, TaskExecutor executor, WorkerAgentRpc agentRpc, Task task) {
        this.scheduledReportPool = scheduledReportPool;
        this.taskRepository = taskRepository;
        this.executor = executor;
        this.agentRpc = agentRpc;
        this.task = task;

        this.status = new AtomicReference<>(Status.WAITING);
    }


    /**
     * 执行任务
     */
    @Override
    public void run() {
        if (!this.status.compareAndSet(Status.WAITING, Status.RUNNING)) {
            log.warn("Task won't execute due to status: [{}]", this.status.get());
            return;
        }

        try {
            ThreadLocalContext.setExecuteContext(this);
            // 开启任务上报
            this.taskReportScheduledFuture = scheduledReportPool.scheduleAtFixedRate(new StatusReportRunnable(task), 1, TaskConstant.TASK_REPORT_SECONDS, TimeUnit.SECONDS);

            // 执行任务
            executor.run(task);

            // 执行成功
            this.status.set(Status.SUCCEED);
            this.agentRpc.feedbackTaskSucceed(task);
        } catch (Exception e) {

            // 执行异常
            log.error("Task execute error", e);
            this.status.set(Status.FAILED);

            this.agentRpc.feedbackTaskFailed(task, e);

        } finally {
            // 最终都要移除任务
            if (taskReportScheduledFuture != null) {
                taskReportScheduledFuture.cancel(true);
            }
            taskRepository.delete(task.getUid());
            ThreadLocalContext.clear();
        }
    }

    private class StatusReportRunnable implements Runnable {

        private Task task;

        public StatusReportRunnable(Task task) {
            this.task = task;
        }

        @Override
        public void run() {
            agentRpc.reportTask(task);
        }
    }


    /**
     * 取消当前任务上下文的执行
     *
     * @return 任务是否被成功取消。如果返回 false，可能是任务已经开始执行，或执行完成。
     */
    public boolean cancel() {
        if (this.scheduleFuture != null) {
            this.scheduleFuture.cancel(true);
        }

        return this.status.compareAndSet(Status.WAITING, Status.CANCELED);
    }

}
