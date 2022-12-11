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

import org.limbo.flowjob.api.constants.HttpWorkerApi;
import org.limbo.flowjob.api.dto.ResponseDTO;
import org.limbo.flowjob.api.param.TaskSubmitParam;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.exceptions.WorkerException;
import org.limbo.flowjob.broker.core.worker.Worker;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

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
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(RetrofitWorkerApi.class);
    }


//    /**
//     * {@inheritDoc}
//     * @return
//     */
//    @Override
//    public WorkerMetric ping() {
//        return WorkerConverter.toDO(send(api.ping()));
//    }

    /**
     * {@inheritDoc}
     *
     * @param task 作业实例
     * @return
     */
    @Override
    public Boolean sendTask(Task task) {

        return send(api.sendTask(WorkerConverter.toTaskSubmitParam(task)));
    }

    private <T> T send(Call<ResponseDTO<T>> call) {
        return getResponseData(() -> {
            try {
                return call.execute().body();
            } catch (Exception e) {
                throw new WorkerException(workerId(), "http api execute error", e);
            }
        });
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

//        @GET(HttpWorkerApi.API_PING)
//        Call<ResponseDTO<WorkerMetricDTO>> ping();

        @Headers(
                "Content-Type: application/json"
        )
        @POST(HttpWorkerApi.API_SEND_TASK)
        Call<ResponseDTO<Boolean>> sendTask(@Body TaskSubmitParam param);

    }

}
