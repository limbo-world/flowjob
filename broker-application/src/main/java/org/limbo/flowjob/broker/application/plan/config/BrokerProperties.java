package org.limbo.flowjob.broker.application.plan.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Devil
 * @since 2021/7/30
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "flowjob.broker")
public class BrokerProperties extends BrokerConfig {

    /**
     * 是否启动 broker
     */
    private boolean enabled = true;

    /**
     * 重分配间隔 毫秒
     */
    private long rebalanceInterval = 10000;
    /**
     * 状态检查间隔 毫秒
     */
    private long statusCheckInterval = 10000;

}
