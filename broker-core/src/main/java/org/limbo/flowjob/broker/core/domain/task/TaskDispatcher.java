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

package org.limbo.flowjob.broker.core.domain.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.constants.enums.TaskStatus;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.exceptions.JobDispatchException;
import org.limbo.flowjob.broker.core.worker.Worker;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2022/9/4
 */
@Slf4j
public class TaskDispatcher {

    /**
     * 将任务下发给worker。
     */
    public static void dispatch(Task task) {
        if (task.getStatus() != TaskStatus.DISPATCHING) {
            throw new JobDispatchException(task.getJobId(), task.getTaskId(), "Cannot startup context due to current status: " + task.getStatus());
        }

        List<Worker> availableWorkers = task.getAvailableWorkers();
        if (CollectionUtils.isEmpty(availableWorkers)) {
            return;
        }
        WorkerSelector workerSelector = WorkerSelectorFactory.newSelector(task.getDispatchOption().getLoadBalanceType());
        for (int i = 0; i < 3; i++) {
            try {
                Worker worker = workerSelector.select(task.getDispatchOption(), task.getExecutorName(), availableWorkers);
                if (worker == null) {
                    return;
                }

                // 发送任务到worker，根据worker返回结果，更新状态
                Boolean dispatched = worker.sendTask(task);
                if (Boolean.TRUE.equals(dispatched)) {

                    // 更新状态
                    task.setStatus(TaskStatus.EXECUTING);
                    task.setWorkerId(worker.getWorkerId());

                    return;
                }

                availableWorkers = availableWorkers.stream().filter(w -> !Objects.equals(w.getWorkerId(), worker.getWorkerId())).collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Task dispatch fail task={}", task, e);
            }
        }

        // 下发失败
        task.setStatus(TaskStatus.FAILED);

    }

}
