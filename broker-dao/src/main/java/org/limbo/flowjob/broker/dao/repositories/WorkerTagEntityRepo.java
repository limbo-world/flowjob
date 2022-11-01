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

package org.limbo.flowjob.broker.dao.repositories;

import org.limbo.flowjob.broker.dao.entity.WorkerTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Brozen
 * @since 2022-09-22
 */
public interface WorkerTagEntityRepo extends JpaRepository<WorkerTagEntity, Long> {


    @Modifying(clearAutomatically = true)
    @Query("delete from WorkerTagEntity where workerId = :workerId")
    int deleteByWorkerId(@Param("workerId") Long workerId);

    List<WorkerTagEntity> findByWorkerId(Long workerId);

}
