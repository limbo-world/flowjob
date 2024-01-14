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

package org.limbo.flowjob.broker.dao.repositories;

import lombok.Setter;
import org.limbo.flowjob.api.constants.InstanceStatus;
import org.limbo.flowjob.api.constants.InstanceType;
import org.limbo.flowjob.broker.core.meta.info.WorkflowJobInfo;
import org.limbo.flowjob.broker.core.meta.instance.DelayInstance;
import org.limbo.flowjob.broker.core.meta.instance.DelayInstanceRepository;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.DelayInstanceEntity;
import org.limbo.flowjob.common.utils.attribute.Attributes;
import org.limbo.flowjob.common.utils.dag.DAG;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

/**
 * @author Devil
 * @since 2024/1/4
 */
@Repository
public class DelayInstanceRepo implements DelayInstanceRepository {

    @Setter(onMethod_ = @Inject)
    private DelayInstanceEntityRepo delayInstanceEntityRepo;

    @Override
    public DelayInstance get(String bizType, String bizId) {
        DelayInstanceEntity instanceEntity = delayInstanceEntityRepo.findByBizTypeAndBizId(bizType, bizId);
        if (instanceEntity == null) {
            return null;
        }
        return assemble(instanceEntity);
    }

    @Override
    public DelayInstance get(String id) {
        DelayInstanceEntity instanceEntity = delayInstanceEntityRepo.findById(id).orElse(null);
        if (instanceEntity == null) {
            return null;
        }
        return assemble(instanceEntity);
    }

    @Override
    @Transactional
    public DelayInstance lockAndGet(String id) {
        DelayInstanceEntity instanceEntity = delayInstanceEntityRepo.selectForUpdate(id);
        if (instanceEntity == null) {
            return null;
        }
        return assemble(instanceEntity);
    }

    @Override
    @Transactional
    public void save(DelayInstance instance) {
        DelayInstanceEntity instanceEntity = new DelayInstanceEntity();
        instanceEntity.setInstanceId(instance.getId());
        instanceEntity.setBizType(instance.getBizType());
        instanceEntity.setBizId(instance.getBizId());
        instanceEntity.setInstanceType(instance.getType().type);
        instanceEntity.setStatus(instance.getStatus().status);
        instanceEntity.setJobInfo(instance.getDag().json());
        instanceEntity.setAttributes(instance.getAttributes().toString());
        instanceEntity.setTriggerAt(instance.getTriggerAt());
        instanceEntity.setStartAt(instance.getStartAt());
        instanceEntity.setFeedbackAt(instance.getFeedbackAt());
        delayInstanceEntityRepo.saveAndFlush(instanceEntity);
    }

    @Override
    @Transactional
    public boolean executing(String instanceId, LocalDateTime startAt) {
        return delayInstanceEntityRepo.executing(instanceId, startAt) > 0;
    }

    @Override
    @Transactional
    public boolean success(String instanceId, LocalDateTime feedbackAt) {
        return delayInstanceEntityRepo.success(instanceId, feedbackAt) > 0;
    }

    @Override
    @Transactional
    public boolean fail(String instanceId, LocalDateTime startAt, LocalDateTime feedbackAt) {
        return delayInstanceEntityRepo.fail(instanceId, startAt, feedbackAt) > 0;
    }

    private DelayInstance assemble(DelayInstanceEntity instanceEntity) {
        if (instanceEntity == null) {
            return null;
        }

        InstanceType type = InstanceType.parse(instanceEntity.getInstanceType());
        InstanceStatus status = InstanceStatus.parse(instanceEntity.getStatus());

        DAG<WorkflowJobInfo> dag = DomainConverter.toJobDag(instanceEntity.getJobInfo());

        return DelayInstance.builder()
                .id(instanceEntity.getInstanceId())
                .bizType(instanceEntity.getBizType())
                .bizId(instanceEntity.getBizId())
                .status(status)
                .type(type)
                .dag(dag)
                .attributes(new Attributes(instanceEntity.getAttributes()))
                .triggerAt(instanceEntity.getTriggerAt())
                .startAt(instanceEntity.getStartAt())
                .feedbackAt(instanceEntity.getFeedbackAt())
                .build();
    }
}
