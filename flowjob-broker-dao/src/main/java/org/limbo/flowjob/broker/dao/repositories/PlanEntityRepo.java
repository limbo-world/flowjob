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
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @author Devil
 * @since 2022/6/22
 */
public interface PlanEntityRepo extends JpaRepository<PlanEntity, String>,
        JpaSpecificationExecutor<PlanEntity>, QuerydslPredicateExecutor<PlanEntity> {

    @Query(value = "select * from flowjob_plan where plan_id = :planId for update", nativeQuery = true)
    PlanEntity selectForUpdate(@Param("planId") String planId);

    @Query(value = "select * from flowjob_plan where plan_id in :planIds and is_enabled = true and is_deleted = false", nativeQuery = true)
    List<PlanEntity> loadPlans(@Param("planIds") List<String> planIds);

    /**
     * 修改过的plan
     */
    @Query(value = "select * from flowjob_plan where plan_id in :planIds and updated_at >= :updatedAt and is_enabled = true and is_deleted = false", nativeQuery = true)
    List<PlanEntity> loadUpdatedPlans(@Param("planIds") List<String> planIds, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanEntity set currentVersion = :newCurrentVersion, recentlyVersion = :newRecentlyVersion, name = :name" +
            " where planId = :planId and currentVersion = :currentVersion and recentlyVersion = :recentlyVersion")
    int updateVersion(@Param("newCurrentVersion") String newCurrentVersion,
                      @Param("newRecentlyVersion") String newRecentlyVersion,
                      @Param("name") String name,
                      @Param("planId") String planId,
                      @Param("currentVersion") String currentVersion,
                      @Param("recentlyVersion") String recentlyVersion);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanEntity set currentVersion = :newCurrentVersion where planId = :planId and currentVersion = :currentVersion ")
    int updateVersion(@Param("newCurrentVersion") String newCurrentVersion,
                      @Param("planId") String planId,
                      @Param("currentVersion") String currentVersion);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanEntity set enabled = :newValue where planId = :planId and enabled = :oldValue")
    int updateEnable(@Param("planId") String planId, @Param("oldValue") boolean oldValue, @Param("newValue") boolean newValue);

    @Modifying(clearAutomatically = true)
    @Query(value = "update flowjob_plan set updated_at = :updatedAt where plan_id = :planId and is_enabled = true and is_deleted = false", nativeQuery = true)
    int updateTime(@Param("planId") String planId, @Param("updatedAt") LocalDateTime updatedAt);

}
