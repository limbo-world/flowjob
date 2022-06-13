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

package org.limbo.flowjob.broker.dao.repositories;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.broker.core.worker.statistics.WorkerStatistics;
import org.limbo.flowjob.broker.core.worker.statistics.WorkerStatisticsRepository;
import org.limbo.flowjob.broker.dao.converter.WorkerStatisticsPoConverter;
import org.limbo.flowjob.broker.dao.entity.WorkerStatisticsEntity;
import org.limbo.flowjob.broker.dao.mybatis.WorkerStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Brozen
 * @since 2021-06-03
 */
@Repository
public class MyBatisWorkerStatisticsRepo implements WorkerStatisticsRepository {

    @Autowired
    private WorkerStatisticsMapper mapper;

    /**
     * {@inheritDoc}
     * @param statistics worker统计记录
     */
    @Override
    public void addOrUpdateWorkerStatistics(WorkerStatistics statistics) {
        WorkerStatisticsEntity po = WorkerStatisticsPoConverter.INSTANCE.convertToPO(statistics);
        Objects.requireNonNull(po);

        int effected = mapper.update(po, Wrappers.<WorkerStatisticsEntity>lambdaUpdate()
                .eq(WorkerStatisticsEntity::getWorkerId, po.getWorkerId()));
        if (effected <= 0) {

            effected = mapper.insertIgnore(po);
            if (effected != 1) {
                throw new IllegalStateException(String.format("Update worker error, effected %s rows", effected));
            }
        }
    }

    /**
     * {@inheritDoc}
     * @param workerId workerId
     * @return
     */
    @Override
    public WorkerStatistics getWorkerStatistics(String workerId) {
        return WorkerStatisticsPoConverter.INSTANCE.convertToDO(mapper.selectById(workerId));
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
