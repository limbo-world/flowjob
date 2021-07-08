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

package org.limbo.flowjob.worker.start.adapter.http.config;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerProtocol;
import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.limbo.flowjob.tracker.commons.dto.job.JobExecuteFeedbackDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerHeartbeatOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.core.infrastructure.AbstractRemoteClient;
import org.limbo.utils.JacksonUtils;
import org.limbo.utils.web.HttpStatus;

import java.io.IOException;

/**
 * @author Devil
 * @date 2021/7/1 11:01 上午
 */
@Slf4j
public class HttpRemoteClient extends AbstractRemoteClient {

    private static OkHttpClient client;

    private String baseUrl;

    public HttpRemoteClient(Worker worker) {
        super(worker);
    }

    @Override
    public void clientStart(String host, int port) {
        baseUrl = "http://" + host;
        if (port > 0) {
            baseUrl = baseUrl + ":" + port;
        }
        client = (new OkHttpClient.Builder()).build();
    }

    @Override
    public void heartbeat(WorkerHeartbeatOptionDto dto) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JacksonUtils.toJSONString(dto));

        Request request = new Request.Builder()
                .url(baseUrl + "/api/worker/v1/heartbeat")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        OkHttpClient client = (new OkHttpClient.Builder()).build();
        Call call = client.newCall(request);
        callAndHandlerResult(call);
    }

    @Override
    public void register(WorkerRegisterOptionDto dto) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JacksonUtils.toJSONString(dto));

        Request request = new Request.Builder()
                .url(baseUrl + "/api/worker/v1")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        OkHttpClient client = (new OkHttpClient.Builder()).build();
        Call call = client.newCall(request);
        callAndHandlerResult(call);
    }

    @Override
    public void jobExecuted(JobExecuteFeedbackDto dto) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JacksonUtils.toJSONString(dto));

        Request request = new Request.Builder()
                .url(baseUrl + "/api/worker/v1/heartbeat")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        Call call = client.newCall(request);
        callAndHandlerResult(call);
    }

    @Override
    public WorkerProtocol getProtocol() {
        return WorkerProtocol.HTTP;
    }

    // todo
    private <T> ResponseDto<T> callAndHandlerResult(Call call) {
        try {
            Response response = call.execute();
            if (!response.isSuccessful()) {
                return null;
            }
            if (response.body() == null) {
                return null;
            }
            ResponseDto<T> responseDto = JacksonUtils.parseObject(response.body().string(), new TypeReference<ResponseDto<T>>() {
            });
            if (HttpStatus.SC_OK != responseDto.getCode()) {

            }
            return responseDto;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
