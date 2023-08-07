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

package org.limbo.flowjob.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.agent.Job;
import org.limbo.flowjob.agent.Task;
import org.limbo.flowjob.agent.dispatch.SimpleWorkerSelectArguments;
import org.limbo.flowjob.agent.dispatch.WorkerSelector;
import org.limbo.flowjob.agent.dispatch.WorkerSelectorFactory;
import org.limbo.flowjob.agent.repository.JobRepository;
import org.limbo.flowjob.agent.repository.TaskRepository;
import org.limbo.flowjob.agent.rpc.AgentBrokerRpc;
import org.limbo.flowjob.agent.rpc.AgentWorkerRpc;
import org.limbo.flowjob.agent.worker.Worker;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.TaskStatus;
import org.limbo.flowjob.api.constants.TaskType;
import org.limbo.flowjob.common.exception.JobException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Devil
 * @since 2023/8/4
 */
@Slf4j
public class JobService {

    private JobRepository jobRepository;

    private TaskRepository taskRepository;

    private AgentBrokerRpc brokerRpc;

    private AgentWorkerRpc workerRpc;

    private WorkerSelectorFactory workerSelectorFactory;

    public JobService(JobRepository jobRepository, TaskRepository taskRepository, AgentBrokerRpc brokerRpc) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.brokerRpc = brokerRpc;
    }

    public boolean save(Job job) {
        return jobRepository.save(job);
    }

    public int count() {
        return jobRepository.count();
    }

    /**
     * 处理 job 调度
     *
     * @param job
     */
    // todo 事务
    public void schedule(Job job) {
        // 根据job类型创建task
        List<Task> tasks = createTasks(job);

        // 如果可以创建的任务为空（只有广播任务）
        if (CollectionUtils.isEmpty(tasks)) {
            handleJobSuccess(job);
            return;
        }

        saveTasks(tasks);

        for (Task task : tasks) {
            boolean dispatched = workerRpc.dispatch(task);
            if (!dispatched) {
                // todo 如果下发失败，需要处理
            }
        }

        // todo 更新本地 task 状态

        brokerRpc.notifyJobDispatched(job.getId());
    }

    /**
     * 通知/更新job状态
     *
     * @param job
     */
    private void handleJobSuccess(Job job) {

    }

    /**
     * 保存task信息
     *
     * @param tasks 列表
     */
    public boolean saveTasks(List<Task> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return true;
        }
        try {
            return taskRepository.batchSave(tasks);
        } catch (Exception e) {
            log.error("batchSave tasks({}) failed.", tasks, e);
        }
        return false;
    }

    public List<Task> createTasks(Job job) {
        List<Worker> workers = brokerRpc.availableWorkers(job.getId());
        List<Worker> dispatchWorker = selectDispatchWorkers();
        SimpleWorkerSelectArguments args = new SimpleWorkerSelectArguments(task);
        WorkerSelector workerSelector = workerSelectorFactory.newSelector(null);
        List<Task> tasks = new ArrayList<>();
        switch (job.getType()) {
            case STANDALONE:
                Worker select = workerSelector.select(args, dispatchWorker);
                tasks.add(createTask(job, TaskType.BROADCAST, select.getId(), null));
                break;
            case BROADCAST:
                for (Worker worker : dispatchWorker) {
                    tasks.add(createTask(job, TaskType.BROADCAST, worker.getId(), null));
                }
                break;
            case MAP:
            case MAP_REDUCE:
                Worker select = workerSelector.select(args, dispatchWorker);
                tasks.add(createTask(job, TaskType.BROADCAST, select.getId(), null));
                break;
            default:
                throw new JobException(job.getId(), MsgConstants.UNKNOWN + " job type:" + job.getType().type);
        }

        return tasks;
    }

    private List<Worker> selectDispatchWorkers(List<Worker> workers) {


    }

    private Task createTask(Job job, TaskType type, String workerId, LocalDateTime triggerAt) {
        Task task = new Task();
        task.setJobId(job.getId());
        task.setType(type);
        task.setStatus(TaskStatus.SCHEDULING);
        task.setExecutorName(job.getExecutorName());
        task.setContext(job.getContext());
        task.setJobAttributes(job.getAttributes());
        task.setWorkerId(workerId);
        task.setTriggerAt(triggerAt);
        return task;
    }

}
