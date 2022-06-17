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
import org.limbo.flowjob.broker.core.plan.PlanInfo;

import java.util.List;

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
    Long addPlan(Plan plan);


    /**
     * 新增执行计划版本号
     * @param plan 执行计划领域对象
     * @param newVersion 新版本号
     * @return 更新成功则返回新的版本号
     */
    Integer updateVersion(Plan plan, Integer newVersion);


    /**
     * 根据计划ID查询计划
     *
     * @param planId 计划ID
     * @return 计划plan
     */
    Plan get(Long planId);

    /**
     * 根据计划ID查询计划
     *
     * @param planInfoId 计划版本ID
     * @return 计划plan
     */
    Plan getByVersion(Long planId, Long planInfoId);


    /**
     * 查询所有需要被调度的计划。
     * TODO 主节点切换 自动查询可调度任务
     * @return 所有需要被调度的作业计划
     */
    List<Plan> listSchedulablePlans();


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
