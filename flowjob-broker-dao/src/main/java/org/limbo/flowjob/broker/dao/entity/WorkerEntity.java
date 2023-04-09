/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.limbo.flowjob.common.constants.WorkerStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 应用中的一个工作节点
 *
 * @author Brozen
 * @since 2021-06-02
 */
@Setter
@Getter
@Table(name = "flowjob_worker")
@Entity
@DynamicInsert
@DynamicUpdate
public class WorkerEntity extends BaseEntity {

    private static final long serialVersionUID = -3237766932023820195L;

    /**
     * 数据库自增id
     */
    @Column(updatable = false)
    private Long id;

    @Id
    private String workerId;
    /**
     * 所属应用
     */
    private String appId;

    /**
     * 名称 唯一
     */
    private String name;

    /**
     * worker服务使用的通信协议
     */
    private String protocol;

    /**
     * worker服务的通信 host
     */
    private String host;

    /**
     * worker服务的通信端口
     */
    private Integer port;

    /**
     * worker节点状态
     * @see WorkerStatus
     */
    private Byte status;

    /**
     * 是否启用 不启用则不会进行任务下发
     */
    @Column(name = "is_enabled")
    private boolean enabled;

    @Override
    public Object getUid() {
        return workerId;
    }
}
