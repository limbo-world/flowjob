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

import org.limbo.flowjob.broker.dao.entity.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;


/**
 * @author Devil
 * @since 2022/6/22
 */
public interface PlanEntityRepo extends JpaRepository<PlanEntity, Long> {

    @Query(value = "select * from flowjob_plan where id = ?1 for update", nativeQuery = true)
    PlanEntity selectForUpdate(@Param("id") Long id);

    List<PlanEntity> findBySlotInAndIsEnabledAndNextTriggerAtBefore(List<Integer> slots, boolean isEnabled, LocalDateTime nextTriggerAt);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanEntity set currentVersion = :newCurrentVersion, recentlyVersion = :newRecentlyVersion " +
            "where id = :id and currentVersion = :currentVersion and recentlyVersion = :recentlyVersion")
    int updateVersion(@Param("newCurrentVersion") Long newCurrentVersion,
                      @Param("newRecentlyVersion") Long newRecentlyVersion,
                      @Param("id") Long id,
                      @Param("currentVersion") Long currentVersion,
                      @Param("recentlyVersion") Long recentlyVersion);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanEntity set isEnabled = :newValue where id = :id and isEnabled = :oldValue")
    int updateEnable(@Param("id") Long id, @Param("oldValue") Boolean oldValue, @Param("newValue") Boolean newValue);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanEntity set nextTriggerAt = :nextTriggerAt where id = :id")
    int nextTriggerAt(@Param("id") Long id, @Param("nextTriggerAt") LocalDateTime nextTriggerAt);
}
