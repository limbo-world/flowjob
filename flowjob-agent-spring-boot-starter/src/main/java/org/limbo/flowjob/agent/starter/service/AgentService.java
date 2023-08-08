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

package org.limbo.flowjob.agent.starter.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.limbo.flowjob.agent.Job;
import org.limbo.flowjob.agent.ScheduleAgent;
import org.limbo.flowjob.agent.Task;
import org.limbo.flowjob.agent.repository.TaskRepository;
import org.limbo.flowjob.api.constants.ExecuteResult;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.param.agent.JobSubmitParam;
import org.limbo.flowjob.api.param.agent.TaskFeedbackParam;
import org.limbo.flowjob.api.param.agent.TaskSubmitParam;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author Devil
 * @since 2023/8/7
 */
@Slf4j
@Service("fjaAgentService")
@AllArgsConstructor
public class AgentService {

    private ScheduleAgent agent;

    private TaskRepository taskRepository;

    public boolean receive(JobSubmitParam param) {
        log.info("receive job param={}", param);
        try {
            Job job = convert(param);
            agent.receiveJob(job);
            return true;
        } catch (Exception e) {
            log.error("Failed to receive job param={}", param, e);
            return false;
        }
    }

    public boolean receive(TaskSubmitParam param) {
        log.info("receive task param={}", param);
        try {
            Task task = convert(param);
            return taskRepository.batchSave(Collections.singletonList(task));
        } catch (Exception e) {
            log.error("Failed to receive task param={}", param, e);
            return false;
        }
    }

    public void taskFeedback(String taskId, TaskFeedbackParam param) {
        ExecuteResult result = param.getResult();
        if (log.isDebugEnabled()) {
            log.debug("receive task feedback id:{} result:{}", taskId, result);
        }

        Task task = taskRepository.getById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("task is null id:" + taskId);
        }

        switch (result) {
            case SUCCEED:
                task.setContext(new Attributes(param.getContext()));
                task.setJobAttributes(new Attributes(param.getJobAttributes()));
                agent.taskSuccess(task, param.getResultData());
                break;

            case FAILED:
                agent.taskFail(task, param.getErrorMsg(), param.getErrorStackTrace());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");

            default:
                throw new IllegalStateException("Unexpect execute result: " + param.getResult());
        }
    }

    public Job convert(JobSubmitParam param) {
        Job job = new Job();
        job.setJobInstanceId(param.getJobInstanceId());
        job.setType(JobType.parse(param.getType()));
        job.setExecutorName(param.getExecutorName());
        job.setContext(new Attributes(param.getContext()));
        job.setAttributes(new Attributes(param.getAttributes()));
        return job;
    }

    public Task convert(TaskSubmitParam param) {
        Task task = new Task();
        return task;
    }

}
