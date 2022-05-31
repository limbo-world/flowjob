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

package org.limbo.flowjob.broker.dao.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.limbo.flowjob.broker.dao.po.WorkerExecutorPO;

import java.util.List;

/**
 * @author Brozen
 * @since 2021-07-05
 */
public interface WorkerExecutorMapper extends BaseMapper<WorkerExecutorPO> {

    /**
     * 批量插入worker执行器
     * @param executors 执行器po列表
     */
    int batchInsert(@Param("executors") List<WorkerExecutorPO> executors);

    /**
     * 根据workerId查询所有执行器
     */
    @Select("SELECT * FROM flowjob_worker_executor WHERE worker_id = #{workerId}")
    List<WorkerExecutorPO> findByWorker(@Param("workerId") String workerId);

}
