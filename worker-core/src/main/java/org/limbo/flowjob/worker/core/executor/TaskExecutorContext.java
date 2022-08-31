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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.limbo.flowjob.broker.api.clent.param.TaskFeedbackParam;
import org.limbo.flowjob.broker.api.constants.enums.ExecuteResult;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.rpc.BrokerRpc;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class TaskExecutorContext implements Runnable {

    private final TaskRepository taskRepository;

    private final TaskExecutor executor;

    private final BrokerRpc brokerRpc;

    private final Task task;

    public TaskExecutorContext(TaskRepository taskRepository, TaskExecutor executor, BrokerRpc brokerRpc, Task task) {
        this.taskRepository = taskRepository;
        this.executor = executor;
        this.brokerRpc = brokerRpc;
        this.task = task;
    }


    /**
     * 运行此任务上下文
     */
    public void start() {
        // 有个相同 id 的任务在执行，则忽略
        if (!taskRepository.save(task.getId(), this)) {
            return;
        }

        // 新建线程 执行当前任务
        new Thread(this).start();
    }


    /**
     * 执行任务
     */
    @Override
    public void run() {
        TaskFeedbackParam feedbackParam = new TaskFeedbackParam();
        feedbackParam.setTaskId(task.getTaskId());

        try {
            // 执行任务
            executor.run(task);

            // 执行成功
            feedbackParam.setResult((int) ExecuteResult.SUCCEED.result);

        } catch (Exception e) {

            // 执行异常
            log.error("Task execute error", e);
            feedbackParam.setErrorStackTrace(ExceptionUtils.getStackTrace(e));
            feedbackParam.setResult((int) ExecuteResult.FAILED.result);

        } finally {

            // 返回执行结果，超时等情况不需要在 Worker 侧考虑
            brokerRpc.feedbackTask(feedbackParam);

            // 最终都要移除任务
            taskRepository.delete(task.getId());
        }
    }

}
