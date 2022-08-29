/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.worker.rpc;

import org.limbo.flowjob.broker.api.clent.dto.TaskReceiveDTO;
import org.limbo.flowjob.broker.api.dto.ResponseDTO;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.exceptions.TaskReceiveException;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

import java.io.IOException;

/**
 * @author Brozen
 * @since 2022-08-26
 */
public class RetrofitHttpWorkerRpc extends HttpWorkerRpc {

    private final RetrofitWorkerApi api;

    public RetrofitHttpWorkerRpc(Worker worker) {
        super(worker);
        this.api = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .build().create(RetrofitWorkerApi.class);
    }


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public WorkerMetric ping() {
        // TODO ???
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @param task 作业实例
     * @return
     * @throws TaskReceiveException
     */
    @Override
    public TaskReceiveDTO sendTask(Task task) throws TaskReceiveException {
        try {
            ResponseDTO<TaskReceiveDTO> response = this.api.sendTask(task).execute().body();
            if (response == null || !response.isOk()) {
                String msg = response == null ? "Unknown error" : response.getMessage();
                throw new TaskReceiveException(task.getJobId(), getWorkerId(), msg);
            }
            return response.getData();
        } catch (IOException e) {
            throw new TaskReceiveException(task.getJobId(), getWorkerId(), "", e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void unregister() {
        // TODO ???
    }


    /**
     * Worker HTTP 协议通信接口
     */
    interface RetrofitWorkerApi {

        @GET(API_PING)
        Call<ResponseDTO<Void>> ping();

        @POST(API_SEND_TASK)
        Call<ResponseDTO<TaskReceiveDTO>> sendTask(@Body Task task);

    }

}
