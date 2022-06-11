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
import org.limbo.flowjob.broker.dao.entity.WorkerStatisticsEntity;

/**
 * @author Brozen
 * @since 2021-06-02
 */
public interface WorkerStatisticsMapper extends BaseMapper<WorkerStatisticsEntity> {


    /**
     * 新增worker统计记录，使用insert ignore语句
     * @param statistics worker 统计记录
     * @return 影响的记录条数
     */
    int insertIgnore(WorkerStatisticsEntity statistics);

}
