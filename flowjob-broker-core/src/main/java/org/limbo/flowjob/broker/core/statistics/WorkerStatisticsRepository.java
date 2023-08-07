/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.statistics;

import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.common.lb.LBServerStatistics;
import org.limbo.flowjob.common.lb.LBServerStatisticsProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2022-12-21
 */
public interface WorkerStatisticsRepository extends LBServerStatisticsProvider {


    /**
     * 记录任务被下发
     *
     * @param worker 接收任务的 worker
     */
    void recordDispatched(Worker worker);


    /**
     * {@inheritDoc}
     * @param serverIds 服务 ID 结合
     * @param interval 查询的统计信息时长
     * @return
     */
    @Override
    default List<LBServerStatistics> getStatistics(Set<String> serverIds, Duration interval) {
        Instant limit = Instant.now().plusSeconds(-interval.getSeconds());
        return list(serverIds, limit).stream()
                .map(i -> ((LBServerStatistics) i))
                .collect(Collectors.toList());
    }


    /**
     * 批量查询某一时间点后的 Worker 统计数据
     *
     * @param workerIds Worker ID 集合
     * @param limit 查询时间点
     */
    List<WorkerLBStatistics> list(Set<String> workerIds, Instant limit);


}
