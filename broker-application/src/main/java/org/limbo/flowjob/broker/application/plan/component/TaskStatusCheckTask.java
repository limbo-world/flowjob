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

package org.limbo.flowjob.broker.application.plan.component;

import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.api.constants.enums.TaskStatus;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.domain.task.TaskDispatcher;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.domain.SlotManager;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanInfoEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * task 如果长时间执行中没有进行反馈 需要对其进行状态检查
 * 可能导致task没有完成的原因
 * 1. worker服务真实下线
 * 2. worker服务假死
 * 3. worker完成task调用broker的接口失败
 */
@Component
public class TaskStatusCheckTask extends TimerTask {

    @Setter(onMethod_ = @Inject)
    private WorkerRepository workerRepository;

    @Setter(onMethod_ = @Inject)
    private WorkerSelector workerSelector;

    @Setter(onMethod_ = @Inject)
    private NodeManger nodeManger;

    @Setter(onMethod_ = @Inject)
    private BrokerConfig config;

    @Setter(onMethod_ = @Inject)
    private PlanInfoEntityRepo planInfoEntityRepo;

    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanSlotEntityRepo planSlotEntityRepo;

    @Setter(onMethod_ = @Inject)
    private TaskDispatcher taskDispatcher;

    @Override
    public void run() {
        List<Integer> slots = SlotManager.slots(nodeManger.allAlive(), config.getHost(), config.getPort());
        if (CollectionUtils.isEmpty(slots)) {
            return;
        }
        List<PlanSlotEntity> slotEntities = planSlotEntityRepo.findBySlotIn(slots);
        if (CollectionUtils.isEmpty(slotEntities)) {
            return;
        }

        List<TaskEntity> tasks = taskEntityRepo.findByPlanIdInAndStatus(slotEntities.stream().map(PlanSlotEntity::getPlanId).collect(Collectors.toList()), TaskStatus.EXECUTING.status);
        for (TaskEntity taskEntity : tasks) {
            // 获取长时间为执行中的task 判断worker是否已经宕机
            Worker worker = workerRepository.get(taskEntity.getWorkerId());
            if (worker == null || !worker.isAlive()) {
                Task task = DomainConverter.toTask(taskEntity, planInfoEntityRepo); // todo 没有worker
                taskDispatcher.dispatch(task);
            }
        }

    }

}
