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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.limbo.flowjob.broker.api.constants.enums.WorkerStatus;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.limbo.flowjob.broker.dao.converter.WorkerPoConverter;
import org.limbo.flowjob.broker.dao.mybatis.WorkerMapper;
import org.limbo.flowjob.broker.dao.entity.WorkerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 2021-06-02
 */
@Repository
public class MyBatisWorkerRepo implements WorkerRepository {

    @Autowired
    private WorkerMapper mapper;

    @Autowired
    private WorkerPoConverter converter;

    /**
     * {@inheritDoc}
     * @param worker worker节点
     */
    @Override
    public void addWorker(Worker worker) {
        WorkerEntity po = converter.convert(worker);
        Objects.requireNonNull(po);

        // todo id 冲突应该给提示更好
        mapper.insertOrUpdate(po);
    }

    /**
     * {@inheritDoc}
     * @param worker 更新worker
     */
    @Override
    public void updateWorker(Worker worker) {
        WorkerEntity po = converter.convert(worker);
        Objects.requireNonNull(po);

        mapper.update(po, Wrappers.<WorkerEntity>lambdaUpdate()
                .eq(WorkerEntity::getWorkerId, po.getWorkerId()));
    }

    /**
     * {@inheritDoc}
     * @param workerId workerId
     * @return
     */
    @Override
    public Worker getWorker(String workerId) {
        return converter.reverse().convert(mapper.selectById(workerId));
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public List<Worker> availableWorkers() {
        return mapper.selectList(Wrappers.<WorkerEntity>lambdaQuery()
                .eq(WorkerEntity::getStatus, WorkerStatus.RUNNING.status)
                .eq(WorkerEntity::getDeleted, Boolean.FALSE)
        ).stream()
                .map(po -> converter.reverse().convert(po))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * @param workerId 需要被移除的workerId
     */
    @Override
    public void removeWorker(String workerId) {
        WorkerEntity po = mapper.selectById(workerId);
        if (po != null) {
            po.setDeleted(true);
            mapper.updateById(po);
        }
    }
}
