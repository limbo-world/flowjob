package org.limbo.flowjob.broker.application.plan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Devil
 * @since 2021/7/30
 */
@Data
@ConfigurationProperties(prefix = "flowjob.broker")
public class BrokerProperties {

    /**
     * tracker 启动模式 默认为 单机 可选 election cluster
     */
    private String mode;

    /**
     * 主机名 配置可用于给 worker 连接
     */
    private String host;

    /**
     * raft数据存放路径
     */
    private String dataPath;

    private String groupId;

    private String serverAddress;

    private String serverAddressList;

}
