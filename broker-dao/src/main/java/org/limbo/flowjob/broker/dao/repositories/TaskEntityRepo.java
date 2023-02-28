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
import org.limbo.flowjob.common.constants.ConstantsPool;
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
public interface TaskEntityRepo extends JpaRepository<TaskEntity, String> {

    List<TaskEntity> findByJobInstanceIdAndType(String jobInstanceId, Byte type);

    List<TaskEntity> findByPlanIdInAndStatus(List<String> planIds, Byte status);

    @Modifying(clearAutomatically = true)
    @Query(value = "update TaskEntity " +
            " set status = " + ConstantsPool.SCHEDULE_STATUS_DISPATCHING +
            " where taskId = :taskId and status = " + ConstantsPool.SCHEDULE_STATUS_SCHEDULING)
    int dispatching(@Param("taskId") String taskId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update TaskEntity " +
            " set status = " + ConstantsPool.SCHEDULE_STATUS_EXECUTING + ", workerId = :workerId, startAt = :startAt " +
            " where taskId = :taskId and status = " + ConstantsPool.SCHEDULE_STATUS_DISPATCHING)
    int executing(@Param("taskId") String taskId, @Param("workerId") String workerId, @Param("startAt") LocalDateTime startAt);

    @Modifying(clearAutomatically = true)
    @Query(value = "update TaskEntity " +
            " set status = " + ConstantsPool.SCHEDULE_STATUS_EXECUTE_SUCCEED + ", context =:context, jobAttributes =:jobAttributes, result =:result, endAt = :endAt " +
            " where taskId = :taskId and status = " + ConstantsPool.SCHEDULE_STATUS_EXECUTING)
    int success(@Param("taskId") String taskId, @Param("endAt") LocalDateTime endAt, @Param("context") String context, @Param("jobAttributes") String jobAttributes, @Param("result") String result);

    @Modifying(clearAutomatically = true)
    @Query(value = "update TaskEntity " +
            " set status = " + ConstantsPool.SCHEDULE_STATUS_EXECUTE_FAILED + ", errorMsg = :errorMsg, errorStackTrace = :errorStack, endAt = :endAt " +
            " where taskId = :taskId and status = :curStatus")
    int fail(@Param("taskId") String taskId, @Param("curStatus") Byte curStatus, @Param("endAt") LocalDateTime endAt, @Param("errorMsg") String errorMsg, @Param("errorStack") String errorStack);
}
