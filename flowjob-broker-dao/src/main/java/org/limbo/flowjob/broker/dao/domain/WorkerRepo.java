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

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.core.domain.IDGenerator;
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
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private IDGenerator idGenerator;

    /**
     * worker 心跳过期时间 毫秒
     */
    @Getter
    @Value("${flowjob.broker.worker.heartbeat-timeout:5000}")
    private long heartbeatExpireInterval;

    /**
     * {@inheritDoc}
     *
     * @param worker worker节点
     */
    @Override
    @Transactional
    public void save(Worker worker) {
        String workerId = worker.getId();

        WorkerEntity entity = WorkerEntityConverter.toWorkerEntity(worker);
        Objects.requireNonNull(entity);
        entity.setUpdatedAt(TimeUtils.currentLocalDateTime());
        workerEntityRepo.saveAndFlush(entity);

        // Metric 存储
        WorkerMetric metric = worker.getMetric();
        WorkerMetricEntity metricPo = WorkerEntityConverter.toMetricEntity(workerId, metric);
        metricEntityRepo.saveAndFlush(Objects.requireNonNull(metricPo));

        // Executors 存储
        executorEntityRepo.deleteByWorkerId(workerId);
        List<WorkerExecutorEntity> executorPos = WorkerEntityConverter.toExecutorEntities(workerId, worker, idGenerator);
        if (CollectionUtils.isNotEmpty(executorPos)) {
            executorEntityRepo.saveAll(executorPos);
            executorEntityRepo.flush();
        }

        // Tags 存储
        tagEntityRepo.deleteByWorkerId(workerId);
        List<WorkerTagEntity> tagPos = WorkerEntityConverter.toTagEntities(workerId, worker, idGenerator);
        if (CollectionUtils.isNotEmpty(tagPos)) {
            tagEntityRepo.saveAll(tagPos);
            tagEntityRepo.flush();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @param worker worker节点
     */
    @Override
    public void saveMetric(Worker worker) {
        // Metric 存储
        WorkerMetric metric = worker.getMetric();
        WorkerMetricEntity metricPo = WorkerEntityConverter.toMetricEntity(worker.getId(), metric);
        metricEntityRepo.saveAndFlush(Objects.requireNonNull(metricPo));
    }


    /**
     * {@inheritDoc}
     *
     * @param id workerId
     * @return
     */
    @Override
    public Worker get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return workerEntityRepo.findByWorkerIdAndDeleted(id, false)
                .map(this::toWorkerWithLazyInit)
                .orElse(null);
    }

    @Override
    public Worker getByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        return workerEntityRepo.findByNameAndDeleted(name, false)
                .map(this::toWorkerWithLazyInit)
                .orElse(null);
    }

    /**
     * 将 Worker 持久化对象转为领域模型，并为其中的属性设置为懒加载。
     */
    private Worker toWorkerWithLazyInit(WorkerEntity worker) {
        if (worker == null) {
            return null;
        }
        String workerId = worker.getWorkerId();
        return WorkerEntityConverter.toWorker(worker,
                executorEntityRepo.findByWorkerId(workerId),
                tagEntityRepo.findByWorkerId(workerId),
                metricEntityRepo.getOne(workerId)
        );
    }


    /**
     * {@inheritDoc}
     *
     * @param id 需要被移除的workerId
     */
    @Override
    public void delete(String id) {
        Optional<WorkerEntity> workerEntityOptional = workerEntityRepo.findByWorkerIdAndDeleted(id, false);
        if (workerEntityOptional.isPresent()) {
            WorkerEntity workerEntity = workerEntityOptional.get();
            workerEntity.setDeleted(true);
            workerEntityRepo.saveAndFlush(workerEntity);
        }
    }

    @Override
    public List<Worker> findByLastHeartbeatAtBetween(LocalDateTime startTime, LocalDateTime endTime) {
        List<WorkerMetricEntity> workerMetricEntities = metricEntityRepo.findByLastHeartbeatAtBetween(startTime, endTime);
        if (CollectionUtils.isEmpty(workerMetricEntities)) {
            return Collections.emptyList();
        }
        List<String> workerIds = workerMetricEntities.stream().map(WorkerMetricEntity::getWorkerId).collect(Collectors.toList());
        Map<String, WorkerMetricEntity> workerMetricEntityMap = workerMetricEntities.stream().collect(Collectors.toMap(WorkerMetricEntity::getWorkerId, m -> m));

        List<WorkerExecutorEntity> workerExecutorEntities = executorEntityRepo.findByWorkerIdIn(workerIds);
        Map<String, List<WorkerExecutorEntity>> workerExecutorEntityMap = workerExecutorEntities.stream().collect(Collectors.groupingBy(WorkerExecutorEntity::getWorkerId));

        List<WorkerTagEntity> workerTagEntities = tagEntityRepo.findByWorkerIdIn(workerIds);
        Map<String, List<WorkerTagEntity>> workerTagEntityMap = workerTagEntities.stream().collect(Collectors.groupingBy(WorkerTagEntity::getWorkerId));

        List<WorkerEntity> workerEntities = workerEntityRepo.findAllById(workerIds);
        List<Worker> workers = new ArrayList<>();
        for (WorkerEntity workerEntity : workerEntities) {
            String workerId = workerEntity.getWorkerId();
            workers.add(WorkerEntityConverter.toWorker(
                    workerEntity,
                    workerExecutorEntityMap.get(workerId),
                    workerTagEntityMap.get(workerId),
                    workerMetricEntityMap.get(workerId)
            ));
        }
        return workers;
    }

    @Override
    @Transactional
    public boolean updateStatus(String workerId, Integer oldStatus, Integer newStatus) {
        return workerEntityRepo.updateStatus(workerId, oldStatus, newStatus) > 0;
    }

}
