package org.limbo.flowjob.tracker.core.tracker.worker;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.limbo.flowjob.tracker.core.exceptions.JobWorkerException;
import org.limbo.flowjob.tracker.core.exceptions.WorkerException;
import org.limbo.flowjob.tracker.core.job.context.JobContext;
import org.limbo.utils.JacksonUtils;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

/**
 * @author Brozen
 * @since 2021-05-25
 */
public class HttpWorker extends AbstractWorker implements Worker {

    /**
     * 基于Netty的Http客户端
     */
    private HttpClient client;

    public HttpWorker(String id, WorkerProtocol protocol, String ip, Integer port, WorkerStatus status,
                      WorkerMetric metric, HttpClient client, WorkerRepository workerRepository) {
        super(id, protocol, ip, port, status, metric, workerRepository);
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
            throw new WorkerException(getId(), "Worker服务访问失败！");
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
                    setMetric(metric);
                    updateWorker();
                });
    }

    /**
     * {@inheritDoc}
     * @param context 作业执行上下文
     * @return
     * @throws JobWorkerException
     */
    @Override
    public Mono<SendJobResult> sendJobContext(JobContext context) throws JobWorkerException {
        return Mono.from(client.post()
                .uri(workerUri("/job"))
                .response((resp, flux) -> responseReceiver(resp, flux, SendJobResult.class)));
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
