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
import org.limbo.flowjob.broker.api.constants.enums.WorkerStatus;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.limbo.flowjob.broker.dao.converter.WorkerPoConverter;
import org.limbo.flowjob.broker.dao.entity.WorkerEntity;
import org.limbo.flowjob.broker.dao.repositories.WorkerEntityRepo;
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
    private WorkerPoConverter converter;

    /**
     * {@inheritDoc}
     *
     * @param worker worker节点
     */
    @Override
    @Transactional
    public void save(Worker worker) {
        WorkerEntity entity = converter.convert(worker);
        Objects.requireNonNull(entity);
        workerEntityRepo.saveAndFlush(entity);
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
                .map(w -> converter.reverse().convert(w))
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
                .map(po -> converter.reverse().convert(po))
                .collect(Collectors.toList());
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
