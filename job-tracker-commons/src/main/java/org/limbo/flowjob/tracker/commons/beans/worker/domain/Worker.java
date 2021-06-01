package org.limbo.flowjob.tracker.commons.beans.worker.domain;

import lombok.Data;
import org.limbo.flowjob.tracker.commons.beans.worker.valueobject.WorkerMetric;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerProtocol;
import org.limbo.flowjob.tracker.commons.constants.enums.WorkerStatus;

/**
 * worker的轻量级领域对象。
 *
 * @author Brozen
 * @since 2021-05-28
 */
@Data
public class Worker {

    /**
     * worker节点ID
     */
    private String id;

    /**
     * worker服务使用的通信协议，默认为Http协议。
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

}
