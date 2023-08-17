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

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.core.CommonThreadPool;
import org.limbo.flowjob.agent.core.Job;
import org.limbo.flowjob.agent.core.Task;
import org.limbo.flowjob.agent.core.TaskDispatcher;
import org.limbo.flowjob.agent.core.TaskFactory;
import org.limbo.flowjob.agent.core.repository.JobRepository;
import org.limbo.flowjob.agent.core.repository.TaskRepository;
import org.limbo.flowjob.agent.core.rpc.AgentBrokerRpc;
import org.limbo.flowjob.api.constants.TaskType;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/8/4
 */
public class TaskService {

    private final TaskRepository taskRepository;

    private final JobRepository jobRepository;

    private final TaskDispatcher taskDispatcher;

    private final AgentBrokerRpc brokerRpc;

    public TaskService(TaskRepository taskRepository, JobRepository jobRepository, TaskDispatcher taskDispatcher, AgentBrokerRpc brokerRpc) {
        this.taskRepository = taskRepository;
        this.jobRepository = jobRepository;
        this.taskDispatcher = taskDispatcher;
        this.brokerRpc = brokerRpc;
    }

    public Task getById(String jobId, String taskId) {
        return taskRepository.getById(jobId, taskId);
    }

    public List<Task> getByLastReportAtBefore(String lastTime, String startId, Integer limit) {
        return taskRepository.getByLastReportAtBefore(lastTime, startId, limit);
    }

    public boolean batchSave(Collection<Task> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return true;
        }
        return taskRepository.batchSave(tasks);
    }

    public boolean report(String jobId, String taskId) {
        return taskRepository.report(jobId, taskId);
    }

    /**
     * task 成功处理
     */
    public void taskSuccess(Task task, Attributes context, String result) {
        task.setContext(context);
        task.setResult(result);
        boolean updated = taskRepository.success(task);
        if (!updated) { // 已经被更新 无需重复处理
            return;
        }

        Job job = jobRepository.getById(task.getJobId());

        switch (task.getType()) {
            case STANDALONE:
            case REDUCE:
                handleJobSuccess(job);
                break;
            case SHARDING:
                dealShardingTaskSuccess(task);
                break;
            case BROADCAST:
                dealBroadcastTaskSuccess(task, job);
                break;
            case MAP:
                dealMapTaskSuccess(task);
                break;
        }

    }

    /**
     * 将 task 待下发的 subTask 进行异步下发
     */
    public void dealShardingTaskSuccess(Task task) {
        // todo 如果失败了需要处理
        CommonThreadPool.IO.submit(() -> {
            String startId = "";
            List<Task> subTasks = taskRepository.getByJobAndType(task.getJobId(), TaskType.MAP, startId, 1000);
            while (CollectionUtils.isNotEmpty(subTasks)) {
                for (Task subTask : subTasks) {
                    taskDispatcher.dispatch(subTask);
                }
                startId = subTasks.get(subTasks.size() - 1).getTaskId();
                subTasks = taskRepository.getByJobAndType(task.getJobId(), TaskType.MAP, startId, 1000);
            }
        });
    }

    /**
     * 检测是否所有task都已经完成
     * 如果已经完成 通知 job 完成
     */
    public void dealBroadcastTaskSuccess(Task task, Job job) {
        if (taskRepository.countUnSuccess(task.getJobId(), TaskType.BROADCAST) > 0) {
            return; // 交由失败的task 或者后面还在执行的task去做后续逻辑处理
        }
        handleJobSuccess(job);
    }

    private void handleJobSuccess(Job job) {
        brokerRpc.feedbackJobSucceed(job);
        jobRepository.delete(job.getId());
    }

    /**
     * 检测是否所有task都已经完成
     * 如果已经完成 下发 ReduceTask
     */
    public void dealMapTaskSuccess(Task task) {
        if (taskRepository.countUnSuccess(task.getJobId(), TaskType.MAP) > 0) {
            return; // 交由失败的task 或者后面还在执行的task去做后续逻辑处理
        }

        Job job = jobRepository.getById(task.getJobId());
        List<String> results = taskRepository.getAllTaskResult(task.getJobId(), TaskType.MAP);
        List<Map<String, Object>> mapResults = results.stream()
                .map(r -> JacksonUtils.parseObject(r, new TypeReference<Map<String, Object>>() {
                }))
                .collect(Collectors.toList());
        Task reduceTask = TaskFactory.createTask(TaskType.REDUCE.name(), job, mapResults, TaskType.REDUCE, null);
        taskRepository.batchSave(Collections.singletonList(reduceTask));
        taskDispatcher.dispatch(reduceTask);
    }


    /**
     * task失败处理
     */
    public void taskFail(Task task, String errorMsg, String errorStackTrace) {
        if (StringUtils.isBlank(errorMsg)) {
            errorMsg = "";
        }
        if (StringUtils.isBlank(errorStackTrace)) {
            errorStackTrace = "";
        }
        Job job = jobRepository.getById(task.getJobId());
        task.setErrorMsg(errorMsg);
        task.setErrorStackTrace(errorStackTrace);
        taskRepository.fail(task);
        if (brokerRpc.feedbackJobFail(job, errorMsg)) {
            jobRepository.delete(job.getId());
        }
        // 终止其它执行中的task
    }

}
