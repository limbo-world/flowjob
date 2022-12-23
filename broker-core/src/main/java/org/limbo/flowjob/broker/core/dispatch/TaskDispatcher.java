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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.exceptions.JobDispatchException;
import org.limbo.flowjob.broker.core.statistics.WorkerStatisticsRepository;
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

    @Setter
    private WorkerSelectorFactory workerSelectorFactory;

    @Setter
    private WorkerStatisticsRepository statisticsRepository;


    /**
     * 将任务下发给worker。
     * task status -> EXECUTING or FAILED
     */
    public void dispatch(Task task) {
        if (log.isDebugEnabled()) {
            log.debug("Start dispatch task={}", task);
        }

        if (task.getStatus() != TaskStatus.DISPATCHING) {
            throw new JobDispatchException(task.getJobId(), task.getMetaId(), "Cannot startup context due to current status: " + task.getStatus());
        }

        List<Worker> availableWorkers = task.getAvailableWorkers();
        if (CollectionUtils.isEmpty(availableWorkers)) {
            if (log.isDebugEnabled()) {
                log.debug("Dispatch failed, no available workers. task={}", task.getTaskId());
            }
            return;
        }

        // 下发时，重试3次，如超过3次仍下发失败，则本次任务下发失败。
        int dispatchRetryTimes = 3;
        WorkerSelector workerSelector = workerSelectorFactory.newSelector(task.getDispatchOption().getLoadBalanceType());
        for (int i = 0; i < dispatchRetryTimes; i++) {
            try {
                // 选择需要下发任务的 worker，这里有 LB 策略
                Worker worker = workerSelector.select(task.getDispatchOption(), task.getExecutorName(), availableWorkers);
                if (worker == null) {
                    return;
                }

                // 发送任务到worker成功，根据worker返回结果，更新状态
                Boolean dispatched = worker.sendTask(task);
                if (Boolean.TRUE.equals(dispatched)) {
                    onDispatchSucceed(task, worker);
                    return;
                } else {
                    onDispatchToWorkerFailed(task, worker);
                }

                availableWorkers = availableWorkers.stream().filter(w -> !Objects.equals(w.getId(), worker.getId())).collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Task dispatch with error task={}", task.getTaskId(), e);
            }
        }

        // 下发失败
        onDispatchFailed(task);
    }


    /**
     * 下发任务到 worker 成功时的流程
     */
    private void onDispatchSucceed(Task task, Worker worker) {
        // 更新状态
        task.setStatus(TaskStatus.EXECUTING);
        task.setWorkerId(worker.getId());

        // 记录统计数据
        statisticsRepository.recordTaskDispatched(task, worker);

        if (log.isDebugEnabled()) {
            log.debug("Task dispatch success task={}", task.getTaskId());
        }
    }


    /**
     * 下发任务到 worker 失败时的流程
     */
    private void onDispatchToWorkerFailed(Task task, Worker worker) {
        if (log.isDebugEnabled()) {
            log.debug("Task dispatch failed: task={} worker={}", task.getTaskId(), worker.getId());
        }
    }


    /**
     * 下发任务流程整体失败时的处理逻辑，重试后仍失败会触发此逻辑。
     */
    private void onDispatchFailed(Task task) {
        task.setStatus(TaskStatus.FAILED);

        if (log.isDebugEnabled()) {
            log.debug("Task dispatch fail task={}", task.getTaskId());
        }
    }

}
