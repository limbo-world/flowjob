/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Brozen
 * @since 2022-08-29
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 2529092986409475369L;

    /**
     * 记录创建时间
     */
    @Transient
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 记录更新时间
     */
    @Transient
    @Column(updatable = false)
    private LocalDateTime updatedAt;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted")
    private boolean deleted;

    public abstract Object getUid();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }

        BaseEntity that = (BaseEntity) o;
        return getUid() != null && Objects.equals(getUid(), that.getUid());
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(getUid());
    }

}
