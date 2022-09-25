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
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetricRepository;
import org.limbo.flowjob.broker.dao.converter.WorkerEntityConverter;
import org.limbo.flowjob.broker.dao.entity.WorkerMetricEntity;
import org.limbo.flowjob.broker.dao.repositories.WorkerMetricEntityRepo;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Objects;

/**
 * @author Brozen
 * @since 2021-06-03
 */
@Repository
public class WorkerMetricRepo implements WorkerMetricRepository {

    @Setter(onMethod_ = @Inject)
    private WorkerMetricEntityRepo workerMetricEntityRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerEntityConverter converter;

    /**
     * {@inheritDoc}
     *
     * @param metric worker指标信息
     */
    @Override
    public void updateMetric(WorkerMetric metric) {
        WorkerMetricEntity po = converter.toMetricEntity(metric);
        Objects.requireNonNull(po);

        // 新增或插入worker指标
        workerMetricEntityRepo.saveAndFlush(po);
    }

    /**
     * {@inheritDoc}
     *
     * @param workerId workerId
     * @return
     */
    @Override
    public WorkerMetric getMetric(String workerId) {
        // 查询metric
        return workerMetricEntityRepo.findById(workerId)
                .map(converter::toMetric)
                .orElse(null);
    }

}
