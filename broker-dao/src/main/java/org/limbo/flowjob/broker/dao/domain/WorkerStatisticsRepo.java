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

package org.limbo.flowjob.broker.dao.domain;

import lombok.Setter;
import org.limbo.flowjob.broker.core.worker.statistics.WorkerStatistics;
import org.limbo.flowjob.broker.core.worker.statistics.WorkerStatisticsRepository;
import org.limbo.flowjob.broker.dao.converter.WorkerStatisticsPoConverter;
import org.limbo.flowjob.broker.dao.entity.WorkerStatisticsEntity;
import org.limbo.flowjob.broker.dao.repositories.WorkerStatisticsEntityRepo;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Brozen
 * @since 2021-06-03
 */
@Repository
public class WorkerStatisticsRepo implements WorkerStatisticsRepository {

    @Setter(onMethod_ = @Inject)
    private WorkerStatisticsEntityRepo workerStatisticsEntityRepo;

    /**
     * {@inheritDoc}
     * @param statistics worker统计记录
     */
    @Override
    public void addOrUpdateWorkerStatistics(WorkerStatistics statistics) {
        WorkerStatisticsEntity entity = WorkerStatisticsPoConverter.toEntity(statistics);
        Objects.requireNonNull(entity);

        workerStatisticsEntityRepo.saveAndFlush(entity);
    }

    /**
     * {@inheritDoc}
     * @param workerId workerId
     * @return
     */
    @Override
    public WorkerStatistics getWorkerStatistics(String workerId) {
        return workerStatisticsEntityRepo.findById(Long.valueOf(workerId)).map(WorkerStatisticsPoConverter::toDO).orElse(null);
    }

    /**
     * {@inheritDoc}
     * @param workerId
     * @param dispatchAt
     * @return
     */
    @Override
    public boolean updateWorkerDispatchTimes(String workerId, LocalDateTime dispatchAt) {
        // TODO
        return false;
    }
}
