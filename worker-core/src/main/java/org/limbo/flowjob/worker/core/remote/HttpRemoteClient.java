/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   	http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.limbo.flowjob.worker.core.remote;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.limbo.flowjob.broker.api.clent.dto.WorkerRegisterDTO;
import org.limbo.flowjob.broker.api.clent.param.TaskExecuteFeedbackParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerHeartbeatParam;
import org.limbo.flowjob.broker.api.clent.param.WorkerRegisterParam;
import org.limbo.flowjob.broker.api.constants.HttpStatus;
import org.limbo.flowjob.broker.api.constants.enums.WorkerProtocol;
import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.common.utils.json.JacksonUtils;

import java.io.IOException;

/**
 * @author Devil
 * @since 2021/7/24
 */
@Slf4j
public class HttpRemoteClient extends AbstractRemoteClient {

    private static OkHttpClient client;

    private String baseUrl;

    @Override
    public void start(String host, int port) {
        baseUrl = "http://" + host; // todo http https
        if (port > 0) {
            baseUrl = baseUrl + ":" + port;
        }
        client = (new OkHttpClient.Builder()).build();
    }

    @Override
    public ResponseDTO<Void> heartbeat(WorkerHeartbeatParam param) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JacksonUtils.toJSONString(param));

        Request request = new Request.Builder()
                .url(baseUrl + "/api/worker/v1/heartbeat")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        Call call = client.newCall(request);
        return callAndHandlerResult(call, null);
    }

    @Override
    public ResponseDTO<WorkerRegisterDTO> register(WorkerRegisterParam param) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JacksonUtils.toJSONString(param));

        Request request = new Request.Builder()
                .url(baseUrl + "/api/worker/v1")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        Call call = client.newCall(request);
        return callAndHandlerResult(call, new TypeReference<ResponseDTO<WorkerRegisterDTO>>() {
        });
    }

    @Override
    public void taskExecuted(TaskExecuteFeedbackParam param) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JacksonUtils.toJSONString(param));

        Request request = new Request.Builder()
                .url(baseUrl + "/api/worker/v1/task/feedback")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        Call call = client.newCall(request);
        callAndHandlerResult(call, null);
    }

    @Override
    public WorkerProtocol getProtocol() {
        return WorkerProtocol.HTTP;
    }

    // todo
    private <T> ResponseDTO<T> callAndHandlerResult(Call call, TypeReference<ResponseDTO<T>> reference) {
        try {
            Response response = call.execute();
            if (!response.isSuccessful()) {
                return null;
            }
            if (response.body() == null) {
                return null;
            }
            ResponseDTO<T> responseDto;
            if (reference != null) {
                responseDto = JacksonUtils.parseObject(response.body().string(), reference);
            } else {
                responseDto = JacksonUtils.parseObject(response.body().string(), ResponseDTO.class);
            }

            if (HttpStatus.SC_OK != responseDto.getCode()) {

            }
            return responseDto;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
