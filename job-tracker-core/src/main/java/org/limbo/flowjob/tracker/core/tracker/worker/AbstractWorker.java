package org.limbo.flowjob.tracker.core.tracker.worker;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

/**
 * @author Brozen
 * @since 2021-05-25
 */
@Data
@Setter(AccessLevel.PROTECTED)
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public abstract class AbstractWorker implements Worker {

    /**
     * worker节点ID
     */
    private String id;

    /**
     * worker服务的通信协议
     */
    private WorkerProtocol protocol;

    /**
     * worker服务的通信IP
     */
    private String ip;

    /**
     * worker服务的通信端口
     */
    private Integer port;

    /**
     * worker节点状态
     */
    private WorkerStatus status;

    /**
     * 本worker节点最近一次上报的指标信息
     */
    private WorkerMetric metric;

    /**
     * 用户更新worker
     */
    private WorkerRepository repository;

    /**
     * 更新worker
     */
    protected void updateWorker() {
        repository.updateWorker(this);
    }

}
