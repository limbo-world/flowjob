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

import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerStatus;
import org.limbo.flowjob.tracker.commons.dto.ResponseDto;
import org.limbo.flowjob.tracker.commons.dto.job.JobInstanceDto;
import org.limbo.flowjob.tracker.commons.dto.worker.JobReceiveResult;
import org.limbo.flowjob.tracker.commons.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.commons.exceptions.WorkerException;
import org.limbo.flowjob.tracker.commons.utils.TimeUtil;
import org.limbo.flowjob.tracker.core.job.context.Task;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetric;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerMetricRepository;
import org.limbo.flowjob.tracker.core.tracker.worker.statistics.WorkerStatisticsRepository;
import org.limbo.utils.JacksonUtils;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

/**
 * @author Brozen
 * @since 2021-05-25
 */
public class HttpWorker extends Worker {

    /**
     * 基于Netty的Http客户端
     */
    private HttpClient client;

    private final static String BASE_URL = "/api/worker/v1";

    public HttpWorker(HttpClient client, WorkerRepository workerRepository,
                      WorkerMetricRepository metricRepository,
                      WorkerStatisticsRepository workerStatisticsRepository) {
        super(workerRepository, metricRepository, workerStatisticsRepository);
        this.client = client;
    }

    /**
     * 格式化URI，返回 http://{host}:{port}/{path} 的格式，path如果以 / 开头，开头的 / 会被忽略。
     *
     * @return http://{host}:{port}/{path}
     */
    protected String workerUri(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return String.format("http://%s:%s%s", getHost(), getPort(), path);
    }

    /**
     * 处理HttpClient的请求响应，并将响应body反序列化为指定bean类型。通过Jackson进行反序列化。
     *
     * @param response    http响应
     * @param byteBufFlux http body数据
     * @param reference   反序列化的bean的类类型
     * @param <T>         反序列化的bean的类型
     * @return 一个Mono，异步处理
     */
    protected <T> Mono<T> responseReceiver(HttpClientResponse response, ByteBufFlux byteBufFlux, TypeReference<ResponseDto<T>> reference) {
        if (!HttpResponseStatus.OK.equals(response.status())) {
            throw new WorkerException(getWorkerId(), "Worker服务访问失败！");
        }

        return byteBufFlux.aggregate()
                .asString()
                .map(json -> JacksonUtils.parseObject(json, reference).getData());
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
                .response((resp, flux) -> responseReceiver(resp, flux, new TypeReference<ResponseDto<WorkerMetric>>() {
                }))
                .doOnNext(metric -> {
                    // 请求成功时，更新worker
                    getMetricRepository().updateMetric(metric);
                }));
    }

    /**
     * {@inheritDoc}
     *
     * @param task 作业实例
     * @return
     * @throws JobWorkerException
     */
    public Mono<JobReceiveResult> sendTask(Task task) throws JobWorkerException {
        // 生成 dto
        JobInstanceDto dto = convertToDto(task);
        return Mono.from(client
                .headers(headers -> headers.add("Content-type", "application/json"))
                .post()
                .uri(workerUri(BASE_URL + "/task"))
                .send(Mono.just(Unpooled.copiedBuffer(JacksonUtils.toJSONString(dto), CharsetUtil.UTF_8)))
                // 获取请求响应并解析
                .response((resp, flux) -> responseReceiver(resp, flux, new TypeReference<ResponseDto<JobReceiveResult>>() {
                })))
                .doOnNext(result -> {
                    // todo 如果worker接受作业，则更新下发时间
                    if (result.getAccepted()) {
                        // FIXME 此处是transaction script写法了，要不要改成update全部数据？
                        getStatisticsRepository().updateWorkerDispatchTimes(getWorkerId(), TimeUtil.nowLocalDateTime());
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

    private JobInstanceDto convertToDto(Task task) {
        JobInstanceDto dto = new JobInstanceDto();
        dto.setPlanId(task.getPlanId());
        dto.setPlanInstanceId(task.getPlanInstanceId());
        dto.setJobId(task.getJobId());
        dto.setExecutorName(task.getExecutorOption().getName());
        return dto;
    }

}
