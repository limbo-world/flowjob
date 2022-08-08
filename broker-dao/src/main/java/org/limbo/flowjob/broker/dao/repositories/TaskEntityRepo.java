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

package org.limbo.flowjob.broker.dao.repositories;

import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Devil
 * @since 2022/6/24
 */
public interface TaskEntityRepo extends JpaRepository<TaskEntity, Long> {

    List<TaskEntity> findByJobInstanceId(Long jobInstanceId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update TaskEntity set status = :newStatus, workerId = :workerId where id = :id and status = :oldStatus")
    int updateStatus(@Param("id") Long id, @Param("oldStatus") Byte oldStatus, @Param("newStatus") Byte newStatus, @Param("workerId") String workerId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update TaskEntity set status = :newStatus where id = :id and status = :oldStatus")
    int updateStatus(@Param("id") Long id, @Param("oldStatus") Byte oldStatus, @Param("newStatus") Byte newStatus);

    @Modifying(clearAutomatically = true)
    @Query(value = "update TaskEntity set status = :newStatus, errorMsg = :errorMsg, errorStackTrace = :errorStack where id = :id and status = :oldStatus")
    int updateStatusWithError(@Param("id") Long id, @Param("oldStatus") Byte oldStatus, @Param("newStatus") Byte newStatus,
                              @Param("errorMsg") String errorMsg, @Param("errorStack") String errorStack);
}
