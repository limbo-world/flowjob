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

package org.limbo.flowjob.broker.core.repositories;

import org.limbo.flowjob.broker.core.plan.Plan;

/**
 * @author Brozen
 * @since 2021-07-12
 */
public interface PlanRepository {

    /**
     * 新增plan
     *
     * @param plan 执行计划
     * @return 返回plan的id。如果入参Plan中没有指定ID，方法内应当自动生成一个并返回。
     */
    String save(Plan plan);


    /**
     * 新增执行计划版本号
     * @param plan 执行计划领域对象
     * @return 更新成功则返回新的版本号
     */
    String updateVersion(Plan plan);


    /**
     * 根据计划ID查询计划
     *
     * @param planId 计划ID
     * @return 计划plan
     */
    Plan get(String planId);


    /**
     * 启用指定计划
     * @param plan 作业执行计划
     * @return 返回更新的数据条数
     */
    int enablePlan(Plan plan);


    /**
     * 停用指定计划
     * @param plan 作业执行计划
     * @return 返回更新的数据条数
     */
    int disablePlan(Plan plan);

}
