package org.limbo.flowjob.tracker.admin.adapter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Devil
 * @since 2021/7/30
 */
@Data
@ConfigurationProperties(prefix = "flowjob.tracker")
public class TrackerProperties {

    /**
     * tracker 名称 集群中唯一
     */
    private String name;

    /**
     * 集群信息 name=http://host:port,name=http://host:port
     */
    private String cluster;
}
