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

package org.limbo.flowjob.agent.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.agent.core.entity.Task;
import org.limbo.flowjob.agent.core.rpc.AgentBrokerRpc;
import org.limbo.flowjob.agent.core.rpc.AgentWorkerRpc;
import org.limbo.flowjob.api.constants.TaskStatus;

import java.util.List;

/**
 * @author Devil
 * @since 2023/8/8
 */
@Slf4j
public class TaskDispatcher {

    private final AgentBrokerRpc agentBrokerRpc;

    private final AgentWorkerRpc agentWorkerRpc;

    public TaskDispatcher(AgentBrokerRpc agentBrokerRpc, AgentWorkerRpc agentWorkerRpc) {
        this.agentBrokerRpc = agentBrokerRpc;
        this.agentWorkerRpc = agentWorkerRpc;
    }

    /**
     * 将任务下发给worker。
     * task status -> EXECUTING or FAILED
     */
    public boolean dispatch(Task task) {
        if (log.isDebugEnabled()) {
            log.debug("start dispatch task={}", task);
        }

        if (task.getStatus() != TaskStatus.SCHEDULING) {
            return false;
        }

        boolean dispatched = false;
        Worker worker = null;
        try {
            if (task.getWorker() == null) {
                List<Worker> workers = agentBrokerRpc.availableWorkers(task.getJobId(), true, true, true, true);
                if (CollectionUtils.isNotEmpty(workers)) {
                    worker = workers.get(0);
                }
                task.setWorker(worker);
            }

            if (task.getWorker() != null) {
                dispatched = agentWorkerRpc.dispatch(worker, task);
            }

            if (dispatched) {
                log.info("Task dispatch success task={} worker={}", task.getTaskId(), worker);
            } else {
                log.info("Task dispatch failed: task={} worker={}", task.getTaskId(), worker == null ? "" : worker.getId());
            }
        } catch (Exception e) {
            log.error("Task dispatch failed: task={} worker={}", task.getTaskId(), worker == null ? "" : worker.getId(), e);
        }
        return dispatched;
    }

}