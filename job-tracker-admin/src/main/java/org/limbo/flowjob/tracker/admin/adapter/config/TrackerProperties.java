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
     * raft数据存放路径
     */
    private String dataPath;

    private String groupId;

    private String serverAddress;

    private String serverAddressList;

//    /**
//     * tracker 名称 集群中唯一
//     */
//    private String name;
//
//    /**
//     * 集群信息 name=http://host:port,name=http://host:port
//     */
//    private String cluster;
}
