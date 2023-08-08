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
import org.limbo.flowjob.api.constants.Protocol;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.param.agent.TaskFeedbackParam;
import org.limbo.flowjob.common.exception.RegisterFailException;
import org.limbo.flowjob.common.http.OKHttpRpc;
import org.limbo.flowjob.common.lb.BaseLBServer;
import org.limbo.flowjob.worker.core.domain.Task;
import org.limbo.flowjob.worker.core.executor.ExecuteContext;
import org.limbo.flowjob.worker.core.rpc.RpcParamFactory;
import org.limbo.flowjob.worker.core.rpc.WorkerAgentRpc;

import javax.annotation.Nullable;

import static org.limbo.flowjob.api.constants.rpc.HttpBrokerApi.API_JOB_FEEDBACK;

/**
 * @author Devil
 * @since 2023/8/7
 */
public class OkHttpAgentRpc extends OKHttpRpc<BaseLBServer>  implements WorkerAgentRpc {

    private static final String BASE_URL = "http://0.0.0.0:8080";

    private static final Protocol DEFAULT_PROTOCOL = Protocol.HTTP;

    public OkHttpAgentRpc() {
        super(null, null);
    }

    @Override
    public void feedbackTaskSucceed(ExecuteContext context) {
        Task task = context.getTask();
        doFeedbackTask(task.getTaskId(), RpcParamFactory.taskFeedbackParam(task.getContext(), task.getJobAttributes(), task.getResult(), null));
    }

    @Override
    public void feedbackTaskFailed(ExecuteContext context, @Nullable Throwable ex) {
        Task task = context.getTask();
        doFeedbackTask(context.getTask().getTaskId(), RpcParamFactory.taskFeedbackParam(task.getContext(), task.getJobAttributes(), task.getResult(), ex));
    }

    /**
     * 反馈任务执行结果
     */
    private void doFeedbackTask(String taskId, TaskFeedbackParam feedbackParam) {
        ResponseDTO<Void> response = executePost(BASE_URL + API_JOB_FEEDBACK + "?taskId=" + taskId, feedbackParam, new TypeReference<ResponseDTO<Void>>() {
        });

        if (response == null || !response.success()) {
            String msg = response == null ? MsgConstants.UNKNOWN : (response.getCode() + ":" + response.getMessage());
            throw new RegisterFailException("Worker feedback Task failed: " + msg);
        }
    }

}
