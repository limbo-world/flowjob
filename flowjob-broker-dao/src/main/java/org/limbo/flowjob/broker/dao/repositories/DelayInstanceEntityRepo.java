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

import org.limbo.flowjob.api.constants.ConstantsPool;
import org.limbo.flowjob.broker.dao.entity.DelayInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2024/1/4
 */
public interface DelayInstanceEntityRepo extends JpaRepository<DelayInstanceEntity, String>, JpaSpecificationExecutor<DelayInstanceEntity> {

    @Query(value = "select * from flowjob_delay_instance where instance_id = :instanceId for update", nativeQuery = true)
    DelayInstanceEntity selectForUpdate(@Param("instanceId") String instanceId);

    DelayInstanceEntity findByTopicAndKey(String topic, String key);

    @Modifying(clearAutomatically = true)
    @Query(value = "update DelayInstanceEntity set status = " + ConstantsPool.INSTANCE_EXECUTING + ", startAt = :startAt " +
            " where instanceId = :instanceId and status = " + ConstantsPool.INSTANCE_DISPATCHING)
    int executing(@Param("instanceId") String instanceId, @Param("startAt") LocalDateTime startAt);

    @Modifying(clearAutomatically = true)
    @Query(value = "update DelayInstanceEntity set status = " + ConstantsPool.INSTANCE_EXECUTE_SUCCEED + ", feedbackAt = :feedbackAt " +
            " where instanceId = :instanceId and status = " + ConstantsPool.INSTANCE_EXECUTING)
    int success(@Param("instanceId") String instanceId, @Param("feedbackAt") LocalDateTime feedbackAt);

    @Modifying(clearAutomatically = true)
    @Query(value = "update DelayInstanceEntity set status = " + ConstantsPool.INSTANCE_EXECUTE_FAILED + ", startAt =:startAt, feedbackAt = :feedbackAt " +
            " where instanceId = :instanceId ")
    int fail(@Param("instanceId") String instanceId, @Param("startAt") LocalDateTime startAt, @Param("feedbackAt") LocalDateTime feedbackAt);
}
