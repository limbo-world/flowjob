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

package org.limbo.flowjob.broker.core.repositories;

import org.limbo.flowjob.broker.core.plan.Plan;
import org.limbo.flowjob.broker.core.plan.PlanScheduler;

import java.util.List;

/**
 * @author Devil
 * @since 2022/6/20
 */
public interface PlanSchedulerRepository {

    /**
     * 根据计划ID查询计划
     *
     * @param version 计划版本ID
     * @return 计划plan
     */
    PlanScheduler get(String version);


    /**
     * 查询所有需要被调度的计划。
     * TODO 主节点切换 自动查询可调度任务
     * @return 所有需要被调度的作业计划
     */
    List<Plan> listSchedulablePlans();

}
