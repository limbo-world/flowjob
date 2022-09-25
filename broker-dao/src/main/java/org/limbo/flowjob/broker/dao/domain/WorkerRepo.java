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
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.constants.enums.WorkerStatus;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.limbo.flowjob.broker.core.worker.metric.WorkerMetric;
import org.limbo.flowjob.broker.dao.converter.WorkerEntityConverter;
import org.limbo.flowjob.broker.dao.entity.WorkerEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerExecutorEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerMetricEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerTagEntity;
import org.limbo.flowjob.broker.dao.repositories.WorkerEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.WorkerExecutorEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.WorkerMetricEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.WorkerTagEntityRepo;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class WorkerRepo implements WorkerRepository {

    @Setter(onMethod_ = @Inject)
    private WorkerEntityRepo workerEntityRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerMetricEntityRepo metricEntityRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerExecutorEntityRepo executorEntityRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerTagEntityRepo tagEntityRepo;

    @Setter(onMethod_ = @Inject)
    private WorkerEntityConverter converter;

    /**
     * {@inheritDoc}
     *
     * @param worker worker节点
     */
    @Override
    @Transactional
    public void save(Worker worker) {
        WorkerEntity entity = converter.toWorkerEntity(worker);
        Objects.requireNonNull(entity);
        workerEntityRepo.saveAndFlush(entity);

        // Metric 存储
        WorkerMetric metric = worker.getMetric();
        WorkerMetricEntity metricPo = converter.toMetricEntity(metric);
        metricEntityRepo.saveAndFlush(Objects.requireNonNull(metricPo));

        // Executors 存储
        executorEntityRepo.deleteByWorkerId(worker.getWorkerId());
        List<WorkerExecutorEntity> executorPos = converter.toExecutorEntities(worker);
        if (CollectionUtils.isNotEmpty(executorPos)) {
            executorEntityRepo.saveAll(executorPos);
            executorEntityRepo.flush();
        }

        // Tags 存储
        tagEntityRepo.deleteByWorkerId(worker.getWorkerId());
        List<WorkerTagEntity> tagPos = converter.toTagEntities(worker);
        if (CollectionUtils.isNotEmpty(tagPos)) {
            tagEntityRepo.saveAll(tagPos);
            tagEntityRepo.flush();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @param workerId workerId
     * @return
     */
    @Override
    public Worker get(String workerId) {
        if (workerId == null) {
            return null;
        }

        return workerEntityRepo.findById(workerId)
                .map(this::toWorkerWithLazyInit)
                .orElse(null);
    }


    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<Worker> listAvailableWorkers() {
        return workerEntityRepo.findByStatusAndDeleted(WorkerStatus.RUNNING.status, Boolean.FALSE)
                .stream()
                .map(this::toWorkerWithLazyInit)
                .collect(Collectors.toList());
    }


    /**
     * 将 Worker 持久化对象转为领域模型，并为其中的属性设置为懒加载。
     */
    private Worker toWorkerWithLazyInit(WorkerEntity worker) {
        String workerId = worker.getId();
        return converter.toWorker(worker,
                converter.toTags(tagEntityRepo.findByWorkerId(workerId)),
                converter.toExecutors(executorEntityRepo.findByWorkerId(workerId)),
                converter.toMetric(metricEntityRepo.getOne(workerId))
        );
    }


    /**
     * {@inheritDoc}
     *
     * @param workerId 需要被移除的workerId
     */
    @Override
    public void delete(String workerId) {
        Optional<WorkerEntity> workerEntityOptional = workerEntityRepo.findById(workerId);
        if (workerEntityOptional.isPresent()) {
            WorkerEntity workerEntity = workerEntityOptional.get();
            workerEntity.setDeleted(true);
            workerEntityRepo.saveAndFlush(workerEntity);
        }
    }

}
