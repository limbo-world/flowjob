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

package org.limbo.flowjob.broker.dao.converter;

import org.limbo.flowjob.broker.core.worker.statistics.WorkerStatistics;
import org.limbo.flowjob.broker.dao.entity.WorkerStatisticsEntity;

/**
 * @author Brozen
 * @since 2021-06-03
 */
@Deprecated
public class WorkerStatisticsPoConverter {

    public static WorkerStatistics toDO(WorkerStatisticsEntity po) {
        WorkerStatistics workerStatistics = new WorkerStatistics();
        workerStatistics.setWorkerId(po.getWorkerId());
        workerStatistics.setJobDispatchCount(po.getJobDispatchCount());
        workerStatistics.setLatestDispatchTime(po.getLatestDispatchTime());
        return workerStatistics;

    }

    public static WorkerStatisticsEntity toEntity(WorkerStatistics domain) {
        WorkerStatisticsEntity workerStatisticsEntity = new WorkerStatisticsEntity();
        workerStatisticsEntity.setWorkerId(domain.getWorkerId());
        workerStatisticsEntity.setJobDispatchCount(domain.getJobDispatchCount());
        workerStatisticsEntity.setLatestDispatchTime(domain.getLatestDispatchTime());
        return workerStatisticsEntity;
    }
}
