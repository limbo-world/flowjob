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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @author Devil
 * @since 2022/6/22
 */
public interface PlanEntityRepo extends JpaRepository<PlanEntity, String> {

    @Query(value = "select * from flowjob_plan where plan_id = :planId for update", nativeQuery = true)
    PlanEntity selectForUpdate(@Param("planId") String planId);

    /**
     * 根据id找到启动的plan
     */
    List<PlanEntity> findByPlanIdInAndEnabled(List<String> planIds, boolean isEnabled);

    /**
     * 找到变动的plan
     */
    List<PlanEntity> findByPlanIdInAndUpdatedAtAfterAndEnabled(List<String> planIds, LocalDateTime updateTime, boolean isEnabled);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanEntity set currentVersion = :newCurrentVersion, recentlyVersion = :newRecentlyVersion " +
            "where planId = :planId and currentVersion = :currentVersion and recentlyVersion = :recentlyVersion")
    int updateVersion(@Param("newCurrentVersion") String newCurrentVersion,
                      @Param("newRecentlyVersion") String newRecentlyVersion,
                      @Param("planId") String planId,
                      @Param("currentVersion") String currentVersion,
                      @Param("recentlyVersion") String recentlyVersion);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanEntity set enabled = :newValue where planId = :planId and enabled = :oldValue")
    int updateEnable(@Param("planId") String planId, @Param("oldValue") boolean oldValue, @Param("newValue") boolean newValue);

}
