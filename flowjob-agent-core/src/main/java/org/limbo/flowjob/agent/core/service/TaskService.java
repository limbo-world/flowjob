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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.core.TaskDispatcher;
import org.limbo.flowjob.agent.core.TaskFactory;
import org.limbo.flowjob.agent.core.entity.Job;
import org.limbo.flowjob.agent.core.entity.Task;
import org.limbo.flowjob.agent.core.repository.JobRepository;
import org.limbo.flowjob.agent.core.repository.TaskRepository;
import org.limbo.flowjob.agent.core.rpc.AgentBrokerRpc;
import org.limbo.flowjob.api.constants.TaskType;
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.console.TaskDTO;
import org.limbo.flowjob.api.param.console.TaskQueryParam;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2023/8/4
 */
@Slf4j
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

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public TaskDispatcher getTaskDispatcher() {
        return taskDispatcher;
    }

    public boolean batchSave(Collection<Task> tasks) {
        return taskRepository.batchSave(tasks);
    }

    public Set<String> getExistTaskIds(String jobId, Collection<String> taskIds) {
        return taskRepository.getExistTaskIds(jobId, taskIds);
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
                job.handleSuccess();
                break;
            case SHARDING:
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
     * 检测是否所有task都已经完成
     * 如果已经完成 通知 job 完成
     */
    public void dealBroadcastTaskSuccess(Task task, Job job) {
        if (taskRepository.countUnSuccess(task.getJobId(), TaskType.BROADCAST) > 0) {
            return; // 交由失败的task 或者后面还在执行的task去做后续逻辑处理
        }
        job.handleSuccess();
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
        job.handleFail(errorMsg);
        // 终止其它执行中的task
    }

    public PageDTO<TaskDTO> page(TaskQueryParam param) {
        long count = taskRepository.queryCount(param);
        if (count <= 0) {
            return PageDTO.empty(param);
        }

        PageDTO<TaskDTO> page = PageDTO.convertByPage(param);
        page.setTotal(count);

        List<Task> tasks = taskRepository.queryPage(param);
        page.setData(tasks.stream().map(task -> {
            TaskDTO dto = new TaskDTO();
            dto.setTaskId(task.getTaskId());
            dto.setJobInstanceId(task.getJobId());
            dto.setWorkerId(task.getWorker() == null ? "" : task.getWorker().getId());
            dto.setWorkerAddress(task.getWorker() == null ? "" : task.getWorker().address());
            dto.setType(task.getType().type);
            dto.setStatus(task.getStatus().status);
            dto.setContext(task.getContext().toString());
            dto.setJobAttributes(task.getJobAttributes().toString());
            dto.setTaskAttributes(task.getTaskAttributes());
            dto.setResult(task.getResult());
            dto.setErrorMsg(task.getErrorMsg());
            dto.setErrorStackTrace(task.getErrorStackTrace());
            dto.setStartAt(task.getStartAt());
            dto.setEndAt(task.getEndAt());
            return dto;
        }).collect(Collectors.toList()));
        return page;
    }

}