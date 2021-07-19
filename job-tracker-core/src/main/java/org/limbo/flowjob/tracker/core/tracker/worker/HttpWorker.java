/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.tracker.core.tracker.worker;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.limbo.flowjob.tracker.commons.dto.worker.JobReceiveResult;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetric;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerStatus;
import org.limbo.flowjob.tracker.commons.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.commons.exceptions.WorkerException;
import org.limbo.flowjob.tracker.core.job.context.JobInstance;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetricRepository;
import org.limbo.flowjob.tracker.core.tracker.worker.statistics.WorkerStatisticsRepository;
import org.limbo.utils.JacksonUtils;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2021-05-25
 */
public class HttpWorker extends Worker {

    /**
     * 基于Netty的Http客户端
     */
    private HttpClient client;

    public HttpWorker(HttpClient client, WorkerRepository workerRepository,
                      WorkerMetricRepository metricRepository,
                      WorkerStatisticsRepository workerStatisticsRepository) {
        super(workerRepository, metricRepository, workerStatisticsRepository);
        this.client = client;
    }

    /**
     * 格式化URI，返回 http://{ip}:{port}/{path} 的格式，path如果以 / 开头，开头的 / 会被忽略。
     * @return http://{ip}:{port}/{path}
     */
    protected String workerUri(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return String.format("http://%s:%s%s", getIp(), getPort(), path);
    }

    /**
     * 处理HttpClient的请求响应，并将响应body反序列化为指定bean类型。通过Jackson进行反序列化。
     * @param response http响应
     * @param byteBufFlux http body数据
     * @param type 反序列化的bean的类类型
     * @param <T> 反序列化的bean的类型
     * @return 一个Mono，异步处理
     */
    protected <T> Mono<T> responseReceiver(HttpClientResponse response, ByteBufFlux byteBufFlux, Class<T> type) {
        if (!HttpResponseStatus.OK.equals(response.status())) {
            throw new WorkerException(getWorkerId(), "Worker服务访问失败！");
        }

        return byteBufFlux.aggregate()
                .asString()
                .map(json -> JacksonUtils.parseObject(json, type));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Mono<WorkerMetric> ping() {
        return Mono.from(client.get()
                .uri(workerUri("/ping"))
                .response((resp, flux) -> responseReceiver(resp, flux, WorkerMetric.class)))
                .doOnNext(metric -> {
                    // 请求成功时，更新worker
                    getMetricRepository().updateMetric(metric);
                });
    }

    /**
     * {@inheritDoc}
     * @param context 作业执行上下文
     * @return
     * @throws JobWorkerException
     */
    public Mono<JobReceiveResult> sendJobContext(JobInstance context) throws JobWorkerException {
        return Mono.from(client.post()
                .uri(workerUri("/api/v1/worker/job"))
                // 获取请求响应并解析
                .response((resp, flux) -> responseReceiver(resp, flux, JobReceiveResult.class)))
                .doOnNext(result -> {
                    // 如果worker接受作业，则更新下发时间
                    if (result.getAccepted()) {
                        // FIXME 此处是transaction script写法了，要不要改成update全部数据？
                        getStatisticsRepository().updateWorkerDispatchTimes(getWorkerId(), LocalDateTime.now());
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregister() {
        setStatus(WorkerStatus.TERMINATED);
        updateWorker();
    }

}
