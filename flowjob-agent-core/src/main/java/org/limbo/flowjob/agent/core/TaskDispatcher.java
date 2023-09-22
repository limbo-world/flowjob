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
import org.limbo.flowjob.agent.core.entity.Job;
import org.limbo.flowjob.agent.core.entity.Task;
import org.limbo.flowjob.agent.core.repository.JobRepository;
import org.limbo.flowjob.agent.core.repository.TaskRepository;
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

    private final JobRepository jobRepository;

    private final TaskRepository taskRepository;

    private final AgentBrokerRpc agentBrokerRpc;

    private final AgentWorkerRpc agentWorkerRpc;

    public TaskDispatcher(JobRepository jobRepository, TaskRepository taskRepository, AgentBrokerRpc agentBrokerRpc, AgentWorkerRpc agentWorkerRpc) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.agentBrokerRpc = agentBrokerRpc;
        this.agentWorkerRpc = agentWorkerRpc;
    }

    /**
     * 将任务下发给worker。
     * task status -> EXECUTING or FAILED
     */
    public void dispatch(Task task) {
        if (log.isDebugEnabled()) {
            log.debug("start dispatch task={}", task);
        }

        if (task.getStatus() != TaskStatus.SCHEDULING) {
            return;
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
                task.setDispatchFailTimes(task.getDispatchFailTimes() + 1);
                log.info("Task dispatch failed: task={} worker={} times={}", task.getTaskId(), worker, task.getDispatchFailTimes());
                taskRepository.dispatchFail(task.getJobId(), task.getTaskId());
                if (task.getDispatchFailTimes() >= 3) {
                    Job job = jobRepository.getById(task.getJobId());
                    job.taskFail(task, String.format("task dispatch fail over limit last worker is %s", worker == null ? "" : worker), "");
                }
            }
        } catch (Exception e) {
            log.error("Task dispatch failed: task={} worker={}", task.getTaskId(), worker, e);
        }
    }

}
