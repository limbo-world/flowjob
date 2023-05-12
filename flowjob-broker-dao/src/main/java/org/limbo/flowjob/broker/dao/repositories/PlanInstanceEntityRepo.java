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
import org.limbo.flowjob.api.constants.ConstantsPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Devil
 * @since 2022/6/24
 */
public interface PlanInstanceEntityRepo extends JpaRepository<PlanInstanceEntity, String> {

    @Query(value = "select * from flowjob_plan_instance where plan_instance_id = :planInstanceId for update", nativeQuery = true)
    PlanInstanceEntity selectForUpdate(@Param("planInstanceId") String planInstanceId);

    PlanInstanceEntity findByPlanIdAndTriggerAtAndTriggerType(String planId, LocalDateTime triggerAt, Integer triggerType);

    List<PlanInstanceEntity> findByPlanIdInAndTriggerAtLessThanEqualAndStatus(List<String> planIds, LocalDateTime triggerAt, Integer status);

    @Query(value = "select * from flowjob_plan_instance " +
            "where plan_id = :planId and schedule_type = :scheduleType and trigger_type = :triggerType and plan_info_id =:planInfoId " +
            "order by trigger_at desc limit 1", nativeQuery = true)
    PlanInstanceEntity findLatelyTrigger(@Param("planId") String planId, @Param("planInfoId") String planInfoId, @Param("scheduleType") Integer scheduleType, @Param("triggerType") Integer triggerType);

    @Query(value = "select * from flowjob_plan_instance " +
            "where plan_id = :planId and schedule_type = :scheduleType and trigger_type = :triggerType and plan_info_id =:planInfoId " +
            "order by feedback_at desc limit 1", nativeQuery = true)
    PlanInstanceEntity findLatelyFeedback(@Param("planId") String planId, @Param("planInfoId") String planInfoId, @Param("scheduleType") Integer scheduleType, @Param("triggerType") Integer triggerType);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanInstanceEntity set status = " + ConstantsPool.SCHEDULE_STATUS_EXECUTING + ", startAt = :startAt " +
            " where planInstanceId = :planInstanceId and status = " + ConstantsPool.SCHEDULE_STATUS_SCHEDULING)
    int executing(@Param("planInstanceId") String planInstanceId, @Param("startAt") LocalDateTime startAt);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanInstanceEntity set status = " + ConstantsPool.SCHEDULE_STATUS_EXECUTE_SUCCEED + ", feedbackAt = :feedbackAt " +
            " where planInstanceId = :planInstanceId and status = " + ConstantsPool.SCHEDULE_STATUS_EXECUTING)
    int success(@Param("planInstanceId") String planInstanceId, @Param("feedbackAt") LocalDateTime feedbackAt);

    @Modifying(clearAutomatically = true)
    @Query(value = "update PlanInstanceEntity set status = " + ConstantsPool.SCHEDULE_STATUS_EXECUTE_FAILED + ", feedbackAt = :feedbackAt " +
            " where planInstanceId = :planInstanceId and status = " + ConstantsPool.SCHEDULE_STATUS_EXECUTING)
    int fail(@Param("planInstanceId") String planInstanceId, @Param("feedbackAt") LocalDateTime feedbackAt);
}
