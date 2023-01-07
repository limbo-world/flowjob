/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.core.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.exceptions.JobDispatchException;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.common.constants.TaskStatus;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2022/9/4
 */
@Slf4j
public class TaskDispatcher {

    private final WorkerManager workerManager;

    public TaskDispatcher(WorkerManager workerManager) {
        this.workerManager = workerManager;
    }

    /**
     * 将任务下发给worker。
     * task status -> EXECUTING or FAILED
     */
    public boolean dispatch(Task task) {
        if (log.isDebugEnabled()) {
            log.debug("start dispatch task={}", task);
        }

        if (StringUtils.isBlank(task.getWorkerId())) {
            return dispatchWithWorkerId(task);
        } else {
            return dispatchNoWorker(task);
        }
    }

    private boolean dispatchWithWorkerId(Task task) {
        Worker worker = workerManager.get(task.getWorkerId());
        if (worker == null) {
            return false;
        }

        try {
            // 发送任务到worker，根据worker返回结果，更新状态
            boolean dispatched = worker.sendTask(task);
            if (dispatched) {
                // 更新状态
                task.setStatus(TaskStatus.EXECUTING);
                task.setWorkerId(worker.getId());

                if (log.isDebugEnabled()) {
                    log.debug("Task dispatch success task={}", task);
                }
                return true;
            }
        } catch (Exception e) {
            log.error("Task dispatch with error task={}", task, e);
        }

        // 下发失败
        task.setStatus(TaskStatus.FAILED);
        if (log.isDebugEnabled()) {
            log.debug("Task dispatch fail task={}", task);
        }
        return false;
    }

    private boolean dispatchNoWorker(Task task) {
        List<Worker> availableWorkers = workerManager.availableWorkers();
        if (CollectionUtils.isEmpty(availableWorkers)) {
            return false;
        }
        WorkerSelector workerSelector = WorkerSelectorFactory.newSelector(task.getDispatchOption().getLoadBalanceType());
        for (int i = 0; i < 3; i++) {
            try {
                Worker worker = workerSelector.select(task.getDispatchOption(), task.getExecutorName(), availableWorkers);
                if (worker == null) {
                    return false;
                }

                // 发送任务到worker，根据worker返回结果，更新状态
                boolean dispatched = worker.sendTask(task);
                if (dispatched) {

                    // 更新状态
                    task.setStatus(TaskStatus.EXECUTING);
                    task.setWorkerId(worker.getId());

                    if (log.isDebugEnabled()) {
                        log.debug("Task dispatch success task={}", task);
                    }
                    return true;
                }

                availableWorkers = availableWorkers.stream().filter(w -> !Objects.equals(w.getId(), worker.getId())).collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Task dispatch with error task={}", task, e);
            }
        }

        // 下发失败
        task.setStatus(TaskStatus.FAILED);
        if (log.isDebugEnabled()) {
            log.debug("Task dispatch fail task={}", task);
        }
        return false;
    }

}
