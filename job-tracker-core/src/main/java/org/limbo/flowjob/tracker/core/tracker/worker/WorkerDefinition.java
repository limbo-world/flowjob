package org.limbo.flowjob.tracker.core.tracker.worker;

/**
 * worker属性的getter定义
 *
 * @author Brozen
 * @since 2021-05-25
 */
public interface WorkerDefinition {


    /**
     * 返回worker节点ID。
     * @return worker节点ID
     */
    String getId();

    /**
     * 获取worker服务使用的通信协议，默认为Http协议。
     * @return worker服务的通信协议
     */
    WorkerProtocol getProtocol();

    /**
     * 获取worker服务使用的IP地址
     * @return worker服务的通信IP
     */
    String getIp();

    /**
     * 获取worker服务使用的端口
     * @return worker服务的通信端口
     */
    Integer getPort();

    /**
     * 获取worker节点最近一次上报的指标信息。
     * @return 本worker节点最近一次上报的指标信息
     */
    WorkerMetric getMetric();

    /**
     * 获取worker节点状态
     * @return worker节点状态
     */
    WorkerStatus getStatus();

    /**
     * Worker的状态
     */
    enum WorkerStatus {

        /**
         * Worker正常运行中
         */
        RUNNING(1),

        /**
         * Worker熔断中，此状态的Worker无法接受任务，并将等待心跳重连并复活。
         */
        FUSING(2),

        /**
         * Worker已停止。
         */
        TERMINATED(3),

        ;

        public final int status;

        WorkerStatus(int status) {
            this.status = status;
        }
    }

    /**
     * worker服务的通信协议
     */
    enum WorkerProtocol {

        /**
         * HTTP协议通信
         */
        HTTP(1);

        public final int protocol;

        WorkerProtocol(int protocol) {
            this.protocol = protocol;
        }

    }

}
