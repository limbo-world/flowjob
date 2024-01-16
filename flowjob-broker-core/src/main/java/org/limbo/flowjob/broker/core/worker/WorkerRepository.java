/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.worker;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Brozen
 * @since 2021-05-19
 */
public interface WorkerRepository {

    /**
     * 新增一个worker
     * @param worker worker节点
     */
    void save(Worker worker);


    /**
     * 仅更新worker指标相关数据
     * @param worker worker节点
     */
    void saveMetric(Worker worker);


    /**
     * 根据id查询worker
     * @param id workerId
     * @return worker节点
     */
    @Nullable
    Worker get(String id);

    /**
     * 根据名称查询worker
     * @param name 名称
     * @return worker节点
     */
    Worker getByName(String name);


    /**
     * 删除一个worker，软删除
     * @param id 需要被移除的workerId
     */
    void delete(String id);

    List<Worker> findByLastHeartbeatAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    boolean updateStatus(String workerId, Integer oldStatus, Integer newStatus);

}
