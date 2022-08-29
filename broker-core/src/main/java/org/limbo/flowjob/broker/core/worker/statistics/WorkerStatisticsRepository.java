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

package org.limbo.flowjob.broker.core.worker.statistics;

import java.time.LocalDateTime;

/**
 * @author Brozen
 * @since 2021-05-28
 */
public interface WorkerStatisticsRepository {

    /**
     * 新增一个worker统计记录，应该在worker新增的时候才新增
     * @param statistics worker统计记录
     */
    void addOrUpdateWorkerStatistics(WorkerStatistics statistics);

    /**
     * 根据workerId查询统计记录
     * @param workerId workerId
     * @return 入参workerId对应的统计记录
     */
    WorkerStatistics getWorkerStatistics(String workerId);

}
