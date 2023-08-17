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

import org.limbo.flowjob.broker.dao.entity.AgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Devil
 * @since 2022/6/24
 */
public interface AgentEntityRepo extends JpaRepository<AgentEntity, String>, JpaSpecificationExecutor<AgentEntity> {

    AgentEntity findByHostAndPort(String host, Integer port);

    List<AgentEntity> findByAgentIdInAndDeleted(List<String> agentIds, boolean deleted);

    /**
     * 查询状态启动未删除 todo 性能
     */
    List<AgentEntity> findByStatusAndAvailableQueueLimitGreaterThanAndEnabledAndDeleted(Integer status, Integer availableQueueLimit, boolean enabled, boolean deleted);


    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update AgentEntity set status = :newStatus where agentId = :agentId and status = :oldStatus ")
    int updateStatus(@Param("agentId") String agentId, @Param("oldStatus") Integer oldStatus, @Param("newStatus") Integer newStatus);
}
