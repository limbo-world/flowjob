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

import com.google.common.base.Converter;
import org.limbo.flowjob.broker.api.constants.enums.JobExecuteType;
import org.limbo.flowjob.broker.core.worker.metric.WorkerExecutor;
import org.limbo.flowjob.broker.dao.entity.WorkerExecutorEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author Brozen
 * @since 2021-07-05
 */
@Component
public class WorkerExecutorPoConverter extends Converter<WorkerExecutor, WorkerExecutorEntity> {

    /**
     * 将 {@link WorkerExecutor} 转换为 {@link WorkerExecutorEntity}
     * @param vo {@link WorkerExecutor} 值对象
     * @return {@link WorkerExecutorEntity} 持久化对象
     */
    @Override
    protected WorkerExecutorEntity doForward(@Nonnull WorkerExecutor vo) {
        WorkerExecutorEntity po = new WorkerExecutorEntity();
        po.setWorkerId(vo.getWorkerId());
        po.setName(vo.getName());
        po.setDescription(vo.getDescription());
        po.setType(vo.getType().type);
        return po;
    }


    /**
     * 将 {@link WorkerExecutorEntity} 转换为 {@link WorkerExecutor}
     * @param po {@link WorkerExecutorEntity} 持久化对象
     * @return {@link WorkerExecutor} 值对象
     */
    @Override
    protected WorkerExecutor doBackward(@Nonnull WorkerExecutorEntity po) {
        WorkerExecutor vo = new WorkerExecutor();
        vo.setWorkerId(po.getWorkerId());
        vo.setName(po.getName());
        vo.setDescription(po.getDescription());
        vo.setType(JobExecuteType.parse(po.getType()));
        return vo;
    }
}
