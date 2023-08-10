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

import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.agent.Job;
import org.limbo.flowjob.agent.ScheduleAgent;
import org.limbo.flowjob.api.constants.ExecuteResult;
import org.limbo.flowjob.api.constants.JobType;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.param.agent.JobSubmitParam;
import org.limbo.flowjob.api.param.agent.SubTaskCreateParam;
import org.limbo.flowjob.api.param.agent.TaskFeedbackParam;
import org.limbo.flowjob.common.rpc.IHttpHandlerProcessor;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.util.ArrayList;
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
                case API_JOB_SUBMIT:
                    JobSubmitParam jobSubmitParam = JacksonUtils.parseObject(data, JobSubmitParam.class);
                    return ResponseDTO.<Boolean>builder().ok(receive(jobSubmitParam)).build();
                case API_TASK_SUBMIT:
                    List<SubTaskCreateParam> subTaskCreateParams = JacksonUtils.parseObject(data, new TypeReference<List<SubTaskCreateParam>>() {
                    });
                    return ResponseDTO.<Boolean>builder().ok(receiveSubTasks(null, subTaskCreateParams)).build();
                case API_TASK_FEEDBACK:
                    TaskFeedbackParam taskFeedbackParam = JacksonUtils.parseObject(data, TaskFeedbackParam.class);
                    taskFeedback(null, taskFeedbackParam);
                    return ResponseDTO.<Boolean>builder().ok(true).build();
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
        }
    }

    public boolean receiveSubTasks(String taskId, List<SubTaskCreateParam> params) {
        log.info("receive task taskId={} param={}", taskId, params);
        try {
            if (CollectionUtils.isEmpty(params)) {
                return true;
            }
            List<Object> attrs = new ArrayList<>();
            for (SubTaskCreateParam param : params) {
                attrs.add(param.getData());
            }
            agent.receiveSubTasks(taskId, attrs);
            return true;
        } catch (Exception e) {
            log.error("Failed to receive task taskId={} param={}", taskId, params, e);
            return false;
        }
    }

    public void taskFeedback(String taskId, TaskFeedbackParam param) {
        ExecuteResult result = param.getResult();
        if (log.isDebugEnabled()) {
            log.debug("receive task feedback id:{} result:{}", taskId, result);
        }

        switch (result) {
            case SUCCEED:
                agent.taskSuccess(taskId, new Attributes(param.getContext()), param.getResultData());
                break;

            case FAILED:
                agent.taskFail(taskId, new Attributes(param.getContext()), param.getErrorMsg(), param.getErrorStackTrace());
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
        job.setContext(new Attributes(param.getContext()));
        job.setAttributes(new Attributes(param.getAttributes()));
        return job;
    }

}
