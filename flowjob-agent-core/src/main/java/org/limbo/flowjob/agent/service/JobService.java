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
import org.limbo.flowjob.agent.TaskFactory;
import org.limbo.flowjob.agent.dispatch.SimpleWorkerSelectArguments;
import org.limbo.flowjob.agent.dispatch.WorkerFilter;
import org.limbo.flowjob.agent.dispatch.WorkerSelector;
import org.limbo.flowjob.agent.dispatch.WorkerSelectorFactory;
import org.limbo.flowjob.agent.repository.JobRepository;
import org.limbo.flowjob.agent.repository.TaskRepository;
import org.limbo.flowjob.agent.rpc.AgentBrokerRpc;
import org.limbo.flowjob.agent.rpc.AgentWorkerRpc;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.TaskStatus;
import org.limbo.flowjob.api.constants.TaskType;
import org.limbo.flowjob.common.exception.JobException;
import org.limbo.flowjob.common.meta.Worker;

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

    public JobService(JobRepository jobRepository, TaskRepository taskRepository, AgentBrokerRpc brokerRpc,
                      AgentWorkerRpc workerRpc, WorkerSelectorFactory workerSelectorFactory) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.brokerRpc = brokerRpc;
        this.workerRpc = workerRpc;
        this.workerSelectorFactory = workerSelectorFactory;
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
    public void schedule(Job job) {
        // 根据job类型创建task
        List<Task> tasks = createTasks(job);

        // 如果可以创建的任务为空（只有广播任务）
        if (CollectionUtils.isEmpty(tasks)) {
            handleJobSuccess(job);
            return;
        }

        if (saveTasks(tasks)) {
            brokerRpc.notifyJobDispatched(job.getJobInstanceId());

            for (Task task : tasks) {
//                boolean dispatched = workerRpc.dispatch(task);
//                if (dispatched) {
//                    taskRepository.executing(task.getTaskId(), task.getWorkerId());
//                } else {
//                    taskRepository.fail(task.getTaskId(), MsgConstants.DISPATCH_FAIL, null);
//                }
            }
        }
    }

    /**
     * 通知/更新job状态
     *
     * @param job
     */
    private void handleJobSuccess(Job job) {
        brokerRpc.feedbackJobSucceed(job);
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
        List<Worker> workers = brokerRpc.availableWorkers(job.getJobInstanceId());
        SimpleWorkerSelectArguments args = new SimpleWorkerSelectArguments(job.getExecutorName(), job.getDispatchOption(), job.getAttributes());
        WorkerSelector workerSelector = workerSelectorFactory.newSelector(null);
        List<Task> tasks = new ArrayList<>();

        WorkerFilter workerFilter = new WorkerFilter(args, workers)
                .filterExecutor()
                .filterTags();

        switch (job.getType()) {
            case STANDALONE:
                List<Worker> ws = workerFilter.filterResources().get();
                Worker w = workerSelector.select(args, ws);
                if (w != null) {
                    tasks.add(TaskFactory.createTask(job, TaskType.STANDALONE, w.getId(), null));
                }
                break;
            case BROADCAST:
                for (Worker worker : workerFilter.get()) {
                    tasks.add(TaskFactory.createTask(job, TaskType.BROADCAST, worker.getId(), null));
                }
                break;
            case MAP:
            case MAP_REDUCE:
                List<Worker> wsm = workerFilter.filterResources().get();
                Worker wm = workerSelector.select(args, wsm);
                if (wm != null) {
                    tasks.add(TaskFactory.createTask(job, TaskType.SHARDING, wm.getId(), null));
                }
                break;
            default:
                throw new JobException(job.getJobInstanceId(), MsgConstants.UNKNOWN + " job type:" + job.getType().type);
        }

        return tasks;
    }

}
