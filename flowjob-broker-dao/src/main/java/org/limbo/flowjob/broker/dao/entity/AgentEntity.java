/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.limbo.flowjob.api.constants.AgentStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2023/8/4
 */
@Setter
@Getter
@Table(name = "flowjob_agent")
@Entity
@DynamicInsert
@DynamicUpdate
public class AgentEntity extends BaseEntity {

    private static final long serialVersionUID = -6463518329027854352L;

    /**
     * 数据库自增id
     */
    @Column(updatable = false)
    private Long id;

    @Id
    private String agentId;

    /**
     * 服务使用的通信协议
     */
    private String protocol;

    /**
     * 服务的通信 host
     */
    private String host;

    /**
     * 服务的通信端口
     */
    private Integer port;

    /**
     * 节点状态
     *
     * @see AgentStatus
     */
    private Integer status;

    /**
     * 任务队列剩余可排队数
     */
    private Integer availableQueueLimit;

    /**
     * 上次心跳上报时间戳，毫秒
     */
    private LocalDateTime lastHeartbeatAt;

    /**
     * 是否启用 不启用则不会进行任务下发
     */
    @Column(name = "is_enabled")
    private boolean enabled;

    @Override
    public Object getUid() {
        return agentId;
    }
}
