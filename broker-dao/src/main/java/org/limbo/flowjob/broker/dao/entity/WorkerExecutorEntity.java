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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Brozen
 * @since 2021-07-05
 */
@Setter
@Getter
@Table(name = "flowjob_worker_executor")
@Entity
@DynamicInsert
@DynamicUpdate
public class WorkerExecutorEntity extends BaseEntity {

    private static final long serialVersionUID = 7370406980674258946L;

    @Id
    private String workerExecutorId;
    /**
     * worker节点ID
     */
    private String workerId;

    /**
     * 执行器名称
     */
    private String name;

    /**
     * 执行器描述信息
     */
    private String description;

}
