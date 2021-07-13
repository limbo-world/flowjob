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

package org.limbo.flowjob.tracker.dao.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.limbo.flowjob.tracker.dao.po.PlanPO;

/**
 * @author Brozen
 * @since 2021-07-13
 */
public interface PlanMapper extends BaseMapper<PlanPO> {


    /**
     * 插入Plan记录，通过 insert on duplicate key update 语句实现。
     * @param plan 需要新增的Plan
     * @return 影响的记录条数
     */
    int insertOrUpdate(@Param("plan") PlanPO plan);

}
