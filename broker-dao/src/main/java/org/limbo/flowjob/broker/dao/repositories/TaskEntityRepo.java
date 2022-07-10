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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Devil
 * @since 2022/6/24
 */
public interface TaskEntityRepo extends JpaRepository<TaskEntity, Long> {

    long countByJobInstanceIdAndStateInAndResultIn(Long jobInstanceId, List<Byte> statuses, List<Byte> results);

    // todo @B 为啥需要每次都设置workerId 不应该下发了就确定了么 而且没必要每次有result为none的条件吧，只需要执行后有就行 和下面方法可以合并？？
    @Modifying(clearAutomatically = true)
    @Query(value = "update TaskEntity set state = :newState, workerId = :workerId where id = :id and state = :oldState and result = :result")
    int updateState(@Param("id") Long id, @Param("oldState") Byte oldState, @Param("result") Byte result,
                    @Param("newState") Byte newState, @Param("workerId") String workerId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update TaskEntity set state = :newState where id = :id and state = :oldState and result = :result")
    int updateState(@Param("id") Long id, @Param("oldState") Byte oldState,
                    @Param("result") Byte result, @Param("newState") Byte newState);

    @Modifying(clearAutomatically = true)
    @Query(value = "update TaskEntity set state = :newState, result = :result, errorMsg = :errorMsg, errorStackTrace = :errorStack where id = :id and state = :oldState")
    int updateStateWithError(@Param("id") Long id, @Param("oldState") Byte oldState,
                             @Param("result") Byte result, @Param("newState") Byte newState,
                             @Param("errorMsg") String errorMsg, @Param("errorStack") String errorStack);

    @Modifying(clearAutomatically = true)
    @Query(value = "update TaskEntity set state = :newState, result = :newResult where id = :id and state = :oldState and result = :oldResult")
    int updateState(@Param("id") Long id, @Param("oldState") Byte oldState, @Param("oldResult") Byte oldResult,
                    @Param("newState") Byte newState, @Param("newResult") Byte newResult);
}
