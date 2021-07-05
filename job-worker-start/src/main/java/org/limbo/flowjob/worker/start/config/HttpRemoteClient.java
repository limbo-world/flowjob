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

package org.limbo.flowjob.worker.start.config;

import okhttp3.*;
import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.limbo.flowjob.tracker.commons.dto.job.JobExecuteFinishDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerHeartbeatOptionDto;
import org.limbo.flowjob.tracker.commons.dto.worker.WorkerRegisterOptionDto;
import org.limbo.flowjob.worker.core.domain.Worker;
import org.limbo.flowjob.worker.core.infrastructure.AbstractRemoteClient;
import org.limbo.utils.JacksonUtils;

import java.io.IOException;

/**
 * @author Devil
 * @date 2021/7/1 11:01 上午
 */
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
        ResponseDto<WorkerHeartbeatOptionDto> responseDto = ResponseDto.<WorkerHeartbeatOptionDto>builder().data(dto).build();
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JacksonUtils.toJSONString(responseDto));

        Request request = new Request.Builder()
                .url(baseUrl + "/heartbeat")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void register(WorkerRegisterOptionDto dto) {
        ResponseDto<WorkerRegisterOptionDto> responseDto = ResponseDto.<WorkerRegisterOptionDto>builder().data(dto).build();
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JacksonUtils.toJSONString(responseDto));

        Request request = new Request.Builder()
                .url(baseUrl + "/register")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void jobExecuted(JobExecuteFinishDto dto) {
        ResponseDto<JobExecuteFinishDto> responseDto = ResponseDto.<JobExecuteFinishDto>builder().data(dto).build();
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JacksonUtils.toJSONString(responseDto));

        Request request = new Request.Builder()
                .url(baseUrl + "/heartbeat")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
