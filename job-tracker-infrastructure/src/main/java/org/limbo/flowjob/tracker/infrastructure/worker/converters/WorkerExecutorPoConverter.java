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

package org.limbo.flowjob.tracker.infrastructure.worker.converters;

import com.google.common.base.Converter;
import org.limbo.flowjob.broker.api.constants.enums.JobExecuteType;
import org.limbo.flowjob.tracker.core.tracker.worker.metric.WorkerExecutor;
import org.limbo.flowjob.broker.dao.po.WorkerExecutorPO;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author Brozen
 * @since 2021-07-05
 */
@Component
public class WorkerExecutorPoConverter extends Converter<WorkerExecutor, WorkerExecutorPO> {

    /**
     * 将 {@link WorkerExecutor} 转换为 {@link WorkerExecutorPO}
     * @param vo {@link WorkerExecutor} 值对象
     * @return {@link WorkerExecutorPO} 持久化对象
     */
    @Override
    protected WorkerExecutorPO doForward(@Nonnull WorkerExecutor vo) {
        WorkerExecutorPO po = new WorkerExecutorPO();
        po.setWorkerId(vo.getWorkerId());
        po.setName(vo.getName());
        po.setDescription(vo.getDescription());
        po.setType(vo.getType().type);
        return po;
    }


    /**
     * 将 {@link WorkerExecutorPO} 转换为 {@link WorkerExecutor}
     * @param po {@link WorkerExecutorPO} 持久化对象
     * @return {@link WorkerExecutor} 值对象
     */
    @Override
    protected WorkerExecutor doBackward(@Nonnull WorkerExecutorPO po) {
        WorkerExecutor vo = new WorkerExecutor();
        vo.setWorkerId(po.getWorkerId());
        vo.setName(po.getName());
        vo.setDescription(po.getDescription());
        vo.setType(JobExecuteType.parse(po.getType()));
        return vo;
    }
}
