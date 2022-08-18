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

package org.limbo.flowjob.broker.core.repository;

import org.limbo.flowjob.broker.core.domain.plan.Plan;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Brozen
 * @since 2021-07-12
 */
public interface PlanRepository {

    /**
     * 新增plan。新增时无需指定初始version，新增成功后会自动生成。
     *
     * @param plan 执行计划
     * @return 返回plan的id。如果入参Plan中没有指定ID，方法内应当自动生成一个并返回。
     */
    String save(Plan plan);


    /**
     * 根据计划ID查询计划
     *
     * @param planId 计划ID
     * @return 计划plan
     */
    Plan get(String planId);

}
