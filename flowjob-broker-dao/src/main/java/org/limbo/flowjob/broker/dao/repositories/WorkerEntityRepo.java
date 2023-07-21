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

import org.limbo.flowjob.broker.dao.entity.WorkerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author Devil
 * @since 2022/6/24
 */
public interface WorkerEntityRepo extends JpaRepository<WorkerEntity, String>, JpaSpecificationExecutor<WorkerEntity> {

    Optional<WorkerEntity> findByWorkerIdAndDeleted(String id, boolean deleted);

    Optional<WorkerEntity> findByNameAndDeleted(String name, boolean deleted);

    List<WorkerEntity> findByWorkerIdInAndDeleted(List<String> workerIds, boolean deleted);

    /**
     * 根据状态查询未删除worker
     */
    List<WorkerEntity> findByStatusAndEnabledAndDeleted(Integer status, boolean enabled, boolean deleted);

    @Modifying(clearAutomatically = true)
    @Query(value = "update WorkerEntity set status = :newStatus where workerId = :workerId and status = :oldStatus ")
    int updateStatus(@Param("workerId") String workerId, @Param("oldStatus") Integer oldStatus, @Param("newStatus") Integer newStatus);

    @Modifying(clearAutomatically = true)
    @Query(value = "update WorkerEntity set enabled = :newValue where workerId = :workerId and enabled = :oldValue")
    int updateEnable(@Param("workerId") String workerId, @Param("oldValue") boolean oldValue, @Param("newValue") boolean newValue);

}
