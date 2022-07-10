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

import org.limbo.flowjob.broker.api.constants.enums.JobExecuteType;
import org.limbo.flowjob.broker.core.worker.metric.WorkerExecutor;
import org.limbo.flowjob.broker.dao.entity.WorkerExecutorEntity;

/**
 * @author Brozen
 * @since 2021-07-05
 */
public class WorkerExecutorPoConverter {

    public static WorkerExecutor toDO(WorkerExecutorEntity po) {
        WorkerExecutor workerExecutor = new WorkerExecutor();
        workerExecutor.setWorkerId(String.valueOf(po.getWorkerId()));
        workerExecutor.setName(po.getName());
        workerExecutor.setDescription(po.getDescription());
        workerExecutor.setType(JobExecuteType.parse(po.getType()));
        return workerExecutor;

    }

    public static WorkerExecutorEntity toEntity(WorkerExecutor domain) {
        WorkerExecutorEntity workerExecutorEntity = new WorkerExecutorEntity();
        workerExecutorEntity.setWorkerId(Long.valueOf(domain.getWorkerId()));
        workerExecutorEntity.setName(domain.getName());
        workerExecutorEntity.setDescription(domain.getDescription());
        workerExecutorEntity.setType(domain.getType() == null ? null : domain.getType().type);
        return workerExecutorEntity;

    }

}
