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

package org.limbo.flowjob.agent.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.agent.core.Job;
import org.limbo.flowjob.agent.core.Task;
import org.limbo.flowjob.agent.core.TaskDispatcher;
import org.limbo.flowjob.agent.core.TaskFactory;
import org.limbo.flowjob.agent.core.repository.JobRepository;
import org.limbo.flowjob.agent.core.repository.TaskRepository;
import org.limbo.flowjob.agent.core.rpc.AgentBrokerRpc;
import org.limbo.flowjob.agent.core.worker.Worker;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.constants.TaskType;
import org.limbo.flowjob.common.exception.JobException;

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

    private TaskDispatcher taskDispatcher;

    public JobService(JobRepository jobRepository, TaskRepository taskRepository, AgentBrokerRpc brokerRpc,
                      TaskDispatcher taskDispatcher) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.brokerRpc = brokerRpc;
        this.taskDispatcher = taskDispatcher;
    }

    public boolean save(Job job) {
        return jobRepository.save(job);
    }

    public int count() {
        return jobRepository.count();
    }

    public Job getById(String id) {
        return jobRepository.getById(id);
    }

    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    /**
     * 处理 job 调度 todo 执行失败的问题，比如task保存失败等
     *
     * @param job
     */
    public void schedule(Job job) {
        log.info("start schedule job={}", job);

        // 根据job类型创建task
        List<Task> tasks = createRootTasks(job);

        // 如果可以创建的任务为空（只有广播任务）
        if (CollectionUtils.isEmpty(tasks)) {
            handleJobSuccess(job);
            return;
        }

        if (saveTasks(tasks)) {
            for (Task task : tasks) {
                taskDispatcher.dispatch(task);
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
        jobRepository.delete(job.getId());
    }

    /**
     * 保存task信息
     *
     * @param tasks 列表
     */
    private boolean saveTasks(List<Task> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return true;
        }
        try {
            return taskRepository.batchSave(tasks);
        } catch (Exception e) {
            log.error("batchSave tasks({}) failed.", tasks, e);
            return false;
        }
    }

    public List<Task> createRootTasks(Job job) {
        List<Task> tasks = new ArrayList<>();
        switch (job.getType()) {
            case STANDALONE:
                tasks.add(TaskFactory.createTask(job.getType().name(), job, null, TaskType.STANDALONE, null));
                break;
            case BROADCAST:
                List<Worker> workers = brokerRpc.availableWorkers(job.getId(), true, true, false);
                int idx = 1;
                for (Worker worker : workers) {
                    Task task = TaskFactory.createTask(String.valueOf(idx), job, null, TaskType.BROADCAST, worker);
                    tasks.add(task);
                    idx++;
                }
                break;
            case MAP:
            case MAP_REDUCE:
                tasks.add(TaskFactory.createTask(job.getType().name(), job, null, TaskType.SHARDING, null));
                break;
            default:
                throw new JobException(job.getId(), MsgConstants.UNKNOWN + " job type:" + job.getType().type);
        }

        return tasks;
    }

}
