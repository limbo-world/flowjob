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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flowjob_worker")
public class WorkerEntity extends Entity {

    private static final long serialVersionUID = -3237766932023820195L;

    /**
     * worker节点ID，根据ip、host、protocol计算得到
     */
    @TableId(type = IdType.INPUT)
    private String workerId;

    /**
     * worker服务使用的通信协议
     */
    private Byte protocol;

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
     */
    private Byte status;

    /**
     * 节点是否被删除
     */
    private Boolean deleted;

}
