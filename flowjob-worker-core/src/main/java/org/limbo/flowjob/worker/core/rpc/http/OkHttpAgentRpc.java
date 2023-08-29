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

package org.limbo.flowjob.worker.core.rpc.http;

import com.fasterxml.jackson.core.type.TypeReference;
import org.limbo.flowjob.api.constants.MsgConstants;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.param.agent.SubTaskCreateParam;
import org.limbo.flowjob.api.param.agent.TaskFeedbackParam;
import org.limbo.flowjob.api.param.agent.TaskReportParam;
import org.limbo.flowjob.common.exception.RegisterFailException;
import org.limbo.flowjob.common.http.OKHttpRpc;
import org.limbo.flowjob.common.lb.BaseLBServer;
import org.limbo.flowjob.worker.core.domain.SubTask;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.rpc.RpcParamFactory;
import org.limbo.flowjob.worker.core.rpc.WorkerAgentRpc;

import javax.annotation.Nullable;
import java.util.List;

import static org.limbo.flowjob.api.constants.rpc.HttpAgentApi.*;

/**
 * @author Devil
 * @since 2023/8/7
 */
public class OkHttpAgentRpc extends OKHttpRpc<BaseLBServer> implements WorkerAgentRpc {

    public OkHttpAgentRpc() {
        super(null, null);
    }

    @Override
    public Boolean submitSubTasks(Task task, List<SubTask> subTasks) {
        SubTaskCreateParam param = RpcParamFactory.subTaskCreateParam(task.getJobId(), subTasks);

        ResponseDTO<Boolean> response = executePost(task.getAgentRpcUrl() + API_TASK_RECEIVE, param, new TypeReference<ResponseDTO<Boolean>>() {
        });

        if (response == null || !response.success()) {
            String msg = response == null ? MsgConstants.UNKNOWN : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Worker submit sub task failed: " + msg);
        }

        return response.getData();
    }

    @Override
    public Boolean reportTask(Task task) {
        TaskReportParam param = RpcParamFactory.taskReportParam(task.getJobId(), task.getTaskId());

        ResponseDTO<Boolean> response = executePost(task.getAgentRpcUrl() + API_TASK_REPORT, param, new TypeReference<ResponseDTO<Boolean>>() {
        });

        if (response == null || !response.success()) {
            String msg = response == null ? MsgConstants.UNKNOWN : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Worker report task failed: " + msg);
        }

        return response.getData();
    }

    @Override
    public Boolean feedbackTaskSucceed(Task task) {
        TaskFeedbackParam taskFeedbackParam = RpcParamFactory.taskFeedbackParam(task.getJobId(), task.getTaskId(), task.getResult(), null);
        return doFeedbackTask(task, taskFeedbackParam);
    }

    @Override
    public Boolean feedbackTaskFailed(Task task, @Nullable Throwable ex) {
        TaskFeedbackParam taskFeedbackParam = RpcParamFactory.taskFeedbackParam(task.getJobId(), task.getTaskId(), task.getResult(), ex);
        return doFeedbackTask(task, taskFeedbackParam);
    }

    /**
     * 反馈任务执行结果
     */
    private Boolean doFeedbackTask(Task task, TaskFeedbackParam feedbackParam) {
        ResponseDTO<Boolean> response = executePost(task.getAgentRpcUrl() + API_TASK_FEEDBACK, feedbackParam, new TypeReference<ResponseDTO<Boolean>>() {
        });

        if (response == null || !response.success()) {
            String msg = response == null ? MsgConstants.UNKNOWN : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Worker feedback Task failed: " + msg);
        }
        return response.getData();
    }

}
