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
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author Brozen
 * @since 2021-07-05
 */
@Mapper
public interface WorkerExecutorPoConverter {

    WorkerExecutorPoConverter INSTANCE = Mappers.getMapper(WorkerExecutorPoConverter.class);

    WorkerExecutor toDO(WorkerExecutorEntity po);

    WorkerExecutorEntity toEntity(WorkerExecutor domain);

    default JobExecuteType jobExecuteTypeConvert(Byte type) {
        return JobExecuteType.parse(type);
    }

    default Byte jobExecuteTypeConvert(JobExecuteType type) {
        return type == null ? null : type.type;
    }
}
