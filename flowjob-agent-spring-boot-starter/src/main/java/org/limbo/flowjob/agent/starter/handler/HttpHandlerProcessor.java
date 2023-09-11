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

package org.limbo.flowjob.agent.starter.handler;

import io.netty.handler.codec.http.HttpMethod;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.core.ScheduleAgent;
import org.limbo.flowjob.agent.core.entity.Job;
import org.limbo.flowjob.agent.core.service.JobService;
import org.limbo.flowjob.agent.core.service.TaskService;
import org.limbo.flowjob.api.constants.ExecuteResult;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.constants.LoadBalanceType;
import org.limbo.flowjob.api.dto.PageDTO;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.dto.console.TaskDTO;
import org.limbo.flowjob.api.param.agent.JobSubmitParam;
import org.limbo.flowjob.api.param.agent.SubTaskCreateParam;
import org.limbo.flowjob.api.param.agent.TaskFeedbackParam;
import org.limbo.flowjob.api.param.agent.TaskReportParam;
import org.limbo.flowjob.api.param.console.TaskQueryParam;
import org.limbo.flowjob.common.rpc.IHttpHandlerProcessor;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.util.List;

import static org.limbo.flowjob.api.constants.rpc.HttpAgentApi.*;

/**
 * @author Devil
 * @since 2023/8/10
 */
@Slf4j
public class HttpHandlerProcessor implements IHttpHandlerProcessor {

    @Setter
    private ScheduleAgent agent;

    @Setter
    private JobService jobService;

    @Setter
    private TaskService taskService;

    @Override
    public String process(HttpMethod httpMethod, String uri, String data) {
        return JacksonUtils.toJSONString(process0(httpMethod, uri, data));
    }

    private ResponseDTO<?> process0(HttpMethod httpMethod, String uri, String data) {
        if (StringUtils.isBlank(uri)) {
            String msg = "Invalid request, Uri is empty.";
            return ResponseDTO.<Void>builder().notFound(msg).build();
        }

        if (HttpMethod.POST != httpMethod) {
            String msg = "Invalid request, Only POST support.";
            log.info(msg + " uri={}", uri);
            return ResponseDTO.<Void>builder().badRequest(msg).build();
        }

        try {
            switch (uri) {
                case API_JOB_RECEIVE:
                    JobSubmitParam jobSubmitParam = JacksonUtils.parseObject(data, JobSubmitParam.class);
                    return ResponseDTO.<Boolean>builder().ok(receive(jobSubmitParam)).build();
                case API_TASK_EXECUTING:
                    TaskReportParam taskReportExecutingParam = JacksonUtils.parseObject(data, TaskReportParam.class);
                    return ResponseDTO.<Boolean>builder().ok(reportTaskExecuting(taskReportExecutingParam)).build();
                case API_TASK_REPORT:
                    TaskReportParam taskReportParam = JacksonUtils.parseObject(data, TaskReportParam.class);
                    return ResponseDTO.<Boolean>builder().ok(reportTask(taskReportParam)).build();
                case API_TASK_RECEIVE:
                    SubTaskCreateParam subTaskCreateParam = JacksonUtils.parseObject(data, SubTaskCreateParam.class);
                    return ResponseDTO.<Boolean>builder().ok(receiveSubTasks(subTaskCreateParam)).build();
                case API_TASK_FEEDBACK:
                    TaskFeedbackParam taskFeedbackParam = JacksonUtils.parseObject(data, TaskFeedbackParam.class);
                    taskFeedback(taskFeedbackParam);
                    return ResponseDTO.<Boolean>builder().ok(true).build();
                case API_TASK_PAGE:
                    TaskQueryParam taskQueryParam = JacksonUtils.parseObject(data, TaskQueryParam.class);
                    return ResponseDTO.<PageDTO<TaskDTO>>builder().ok(taskService.page(taskQueryParam)).build();
                case "/api/v1/backdoor/job/list":
                    return ResponseDTO.<List<Job>>builder().ok(jobService.findAll()).build();
            }

            String msg = "Invalid request, Uri NotFound.";
            log.info(msg + " uri={}", uri);
            return ResponseDTO.<Void>builder().notFound(msg).build();
        } catch (Exception e) {
            log.error("Request process fail uri={}", uri, e);
            return ResponseDTO.<Void>builder().error(e.getMessage()).build();
        }
    }


    public boolean receive(JobSubmitParam param) {
        log.info("receive job param={}", param);
        try {
            Job job = convert(param);
            agent.receiveJob(job);
            return true;
        } catch (Exception e) {
            log.error("Failed to receive job param={}", param, e);
            return false;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("receive job success param={}", param);
            }
        }
    }

    public boolean reportTaskExecuting(TaskReportParam param) {
        if (log.isDebugEnabled()) {
            log.debug("report task param={}", param);
        }
        try {
            if (param == null) {
                return true;
            }
            agent.reportTaskExecuting(param);
            return true;
        } catch (Exception e) {
            log.error("Failed to report task param={}", param, e);
            return false;
        }
    }

    public boolean reportTask(TaskReportParam param) {
        if (log.isDebugEnabled()) {
            log.debug("report task param={}", param);
        }
        try {
            if (param == null) {
                return true;
            }
            agent.reportTask(param);
            return true;
        } catch (Exception e) {
            log.error("Failed to report task param={}", param, e);
            return false;
        }
    }

    public boolean receiveSubTasks(SubTaskCreateParam param) {
        log.info("receive sub task param={}", param);
        try {
            if (param == null) {
                return true;
            }
            agent.receiveSubTasks(param);
            return true;
        } catch (Exception e) {
            log.error("Failed to receive sub task param={}", param, e);
            return false;
        }
    }

    public void taskFeedback(TaskFeedbackParam param) {
        if (param == null) {
            return;
        }

        ExecuteResult result = param.getResult();
        if (log.isDebugEnabled()) {
            log.debug("receive task feedback param:{}", param);
        }

        switch (result) {
            case SUCCEED:
                agent.taskSuccess(param.getJobId(), param.getTaskId(), new Attributes(param.getContext()), param.getResultData());
                break;

            case FAILED:
                agent.taskFail(param.getJobId(), param.getTaskId(), param.getErrorMsg(), param.getErrorStackTrace());
                break;

            case TERMINATED:
                throw new UnsupportedOperationException("暂不支持手动终止任务");

            default:
                throw new IllegalStateException("Unexpect execute result: " + param.getResult());
        }
    }

    public Job convert(JobSubmitParam param) {
        Job job = new Job();
        job.setId(param.getJobInstanceId());
        job.setType(JobType.parse(param.getType()));
        job.setExecutorName(param.getExecutorName());
        job.setLoadBalanceType(LoadBalanceType.parse(param.getLoadBalanceType()));
        job.setContext(new Attributes(param.getContext()));
        job.setAttributes(new Attributes(param.getAttributes()));
        return job;
    }

}
