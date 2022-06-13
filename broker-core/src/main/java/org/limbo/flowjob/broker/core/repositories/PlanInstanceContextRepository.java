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

import org.limbo.flowjob.broker.api.constants.enums.PlanScheduleStatus;
import org.limbo.flowjob.broker.core.plan.PlanInstanceContext;
import org.limbo.flowjob.broker.core.plan.PlanInstance;

import java.util.List;

/**
 * @author Devil
 * @since 2021/7/24
 */
public interface PlanInstanceContextRepository {

    /**
     * 持久化实例
     * @param instance 实例
     */
    void add(PlanInstanceContext instance);


    /**
     * 实例结束
     */
    void end(PlanInstanceContext.ID planInstanceId, PlanScheduleStatus state);


    /**
     * 获取实例
     * @param planInstanceId 计划实例ID
     * @return 实例
     */
    PlanInstanceContext get(PlanInstanceContext.ID planInstanceId);


    /**
     * 查询计划执行记录关联的所有计划实例
     * @param planRecordId 计划执行记录ID
     */
    List<PlanInstanceContext> list(PlanInstance.ID planRecordId);


    /**
     * 创建ID
     */
    PlanInstanceContext.ID createId(PlanInstance.ID planRecordId);


}
