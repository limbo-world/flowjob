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

package org.limbo.flowjob.broker.application.converter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.dto.broker.WorkerRegisterDTO;
import org.limbo.flowjob.api.dto.console.WorkerDTO;
import org.limbo.flowjob.api.dto.console.WorkerTagDTO;
import org.limbo.flowjob.api.param.broker.WorkerExecutorRegisterParam;
import org.limbo.flowjob.api.param.broker.WorkerRegisterParam;
import org.limbo.flowjob.api.param.broker.WorkerResourceParam;
import org.limbo.flowjob.broker.core.cluster.Node;
import org.limbo.flowjob.broker.dao.entity.WorkerEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerExecutorEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerMetricEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerTagEntity;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Brozen
 * @since 2022-08-12
 */
@Slf4j
public class WorkerConverter {

    public static WorkerMetricEntity toWorkerMetricEntity(String workerId, WorkerResourceParam resource) {
        WorkerMetricEntity workerMetricEntity = new WorkerMetricEntity();
        workerMetricEntity.setWorkerId(workerId);
        workerMetricEntity.setAvailableCpu(resource.getAvailableCpu());
        workerMetricEntity.setAvailableRam(resource.getAvailableRAM());
        workerMetricEntity.setAvailableQueueLimit(resource.getAvailableQueueLimit());
        workerMetricEntity.setLastHeartbeatAt(TimeUtils.currentLocalDateTime());
        return workerMetricEntity;
    }

    public static WorkerExecutorEntity toWorkerExecutorEntity(String workerId, WorkerExecutorRegisterParam param) {
        WorkerExecutorEntity workerExecutorEntity = new WorkerExecutorEntity();
        workerExecutorEntity.setWorkerId(workerId);
        workerExecutorEntity.setName(param.getName());
        workerExecutorEntity.setDescription(param.getDescription());
        return workerExecutorEntity;
    }

    public static WorkerTagEntity toWorkerTagEntity(String workerId, WorkerRegisterParam.Tag tag) {
        WorkerTagEntity workerTagEntity = new WorkerTagEntity();
        workerTagEntity.setWorkerId(workerId);
        workerTagEntity.setTagKey(tag.getKey());
        workerTagEntity.setTagValue(tag.getValue());
        return workerTagEntity;
    }

    /**
     * Worker 注册结果
     */
    public static WorkerRegisterDTO toRegisterDTO(String workerId, Collection<Node> nodes) {
        WorkerRegisterDTO registerResult = new WorkerRegisterDTO();
        registerResult.setWorkerId(workerId);
        registerResult.setBrokerTopology(BrokerConverter.toBrokerTopologyDTO(nodes));
        return registerResult;
    }

    public static WorkerDTO toVO(WorkerEntity workerEntity, List<WorkerTagEntity> workerTagEntities) {
        WorkerDTO workerDTO = new WorkerDTO();
        workerDTO.setWorkerId(workerEntity.getWorkerId());
        workerDTO.setName(workerEntity.getName());
        workerDTO.setProtocol(workerEntity.getProtocol());
        workerDTO.setHost(workerEntity.getHost());
        workerDTO.setPort(workerEntity.getPort());
        workerDTO.setStatus(workerEntity.getStatus());
        workerDTO.setTags(new ArrayList<>());
        if (CollectionUtils.isNotEmpty(workerTagEntities)) {
            for (WorkerTagEntity workerTagEntity : workerTagEntities) {
                workerDTO.getTags().add(new WorkerTagDTO(workerTagEntity.getTagKey(), workerTagEntity.getTagValue()));
            }
        }
        workerDTO.setEnabled(workerEntity.isEnabled());
        return workerDTO;
    }

}
