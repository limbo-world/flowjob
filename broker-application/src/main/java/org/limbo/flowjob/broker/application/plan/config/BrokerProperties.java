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

}
