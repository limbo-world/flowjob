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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 应用 application 由ak/sk提供对外暴露应用下数据控制的能力
 *
 * @author Devil
 * @since 2022/6/24
 */
@Setter
@Getter
@Table(name = "flowjob_app")
@Entity
@DynamicInsert
@DynamicUpdate
public class AppEntity extends BaseEntity {

    private static final long serialVersionUID = 1834852529057424113L;

    /**
     * 数据库自增id
     */
    @Column(updatable = false)
    private Long id;

    @Id
    private String appId;
    /**
     * 所属租户
     */
    private String tenantId;

    /**
     * 应用名称
     */
    private String name;

    /**
     * Access Key
     */
    private String ak;

    /**
     * Secret Access Key
     */
    private String sk;


    @Override
    public Object getUid() {
        return appId;
    }
}
