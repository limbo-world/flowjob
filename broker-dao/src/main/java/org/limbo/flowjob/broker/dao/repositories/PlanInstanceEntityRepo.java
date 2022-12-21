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

import org.limbo.flowjob.broker.dao.entity.PlanInstanceEntity;
import org.limbo.flowjob.common.constants.ConstantsPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2022/6/24
 */
public interface PlanInstanceEntityRepo extends JpaRepository<PlanInstanceEntity, String> {

    PlanInstanceEntity findByPlanIdAndTriggerAtAndTriggerType(String planId, LocalDateTime triggerAt, Byte triggerType);

    @Query(value = "select * from flowjob_plan_instance where plan_id = :planId order by trigger_at desc limit 1", nativeQuery = true)
    PlanInstanceEntity findLatelyTrigger(@Param("planId") String planId);

    @Query(value = "select * from flowjob_plan_instance where plan_id = :planId order by feedback_at desc limit 1", nativeQuery = true)
    PlanInstanceEntity findLatelyFeedback(@Param("planId") String planId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanInstanceEntity set status = " + ConstantsPool.PLAN_STATUS_EXECUTING + ", startAt = :startAt where planInstanceId = :planInstanceId and status = " + ConstantsPool.PLAN_STATUS_SCHEDULING)
    int executing(@Param("planInstanceId") String planInstanceId, @Param("startAt") LocalDateTime startAt);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanInstanceEntity set status = " + ConstantsPool.PLAN_STATUS_SUCCEED + ", feedbackAt = :feedbackAt where planInstanceId = :planInstanceId and status = " + ConstantsPool.PLAN_STATUS_EXECUTING)
    int success(@Param("planInstanceId") String planInstanceId, @Param("feedbackAt") LocalDateTime feedbackAt);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanInstanceEntity set status = " + ConstantsPool.PLAN_STATUS_FAILED + ", feedbackAt = :feedbackAt where planInstanceId = :planInstanceId and status = " + ConstantsPool.PLAN_STATUS_EXECUTING)
    int fail(@Param("planInstanceId") String planInstanceId, @Param("feedbackAt") LocalDateTime feedbackAt);
}
