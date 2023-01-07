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
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.AbstractTaskStatusCheckTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.PlanSlotEntity;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.PlanSlotEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.broker.dao.support.SlotManager;
import org.limbo.flowjob.common.constants.TaskStatus;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * task 如果长时间执行中没有进行反馈 需要对其进行状态检查
 * 可能导致task没有完成的原因
 * 1. worker服务真实下线
 * 2. worker服务假死
 * 3. worker完成task调用broker的接口失败
 */
public class TaskStatusCheckTask extends AbstractTaskStatusCheckTask {

    @Setter(onMethod_ = @Inject)
    private TaskEntityRepo taskEntityRepo;

    @Setter(onMethod_ = @Inject)
    private PlanSlotEntityRepo planSlotEntityRepo;

    @Setter(onMethod_ = @Inject)
    private DomainConverter domainConverter;

    public TaskStatusCheckTask(Duration interval,
                               BrokerConfig config,
                               NodeManger nodeManger,
                               MetaTaskScheduler metaTaskScheduler,
                               WorkerRepository workerRepository) {
        super(interval, config, nodeManger, metaTaskScheduler, workerRepository);
    }


    @Override
    protected List<Task> loadDispatchingTasks() {
        List<String> planIds = loadPlanIds();
        List<TaskEntity> taskEntities = taskEntityRepo.findByPlanIdInAndStatus(planIds, TaskStatus.DISPATCHING.status);
        return taskEntities.stream().map(entity -> domainConverter.toTask(entity)).collect(Collectors.toList());
    }

    @Override
    protected List<Task> loadExecutingTasks() {
        List<String> planIds = loadPlanIds();
        List<TaskEntity> taskEntities = taskEntityRepo.findByPlanIdInAndStatus(planIds, TaskStatus.EXECUTING.status);
        return taskEntities.stream().map(entity -> domainConverter.toTask(entity)).collect(Collectors.toList());
    }

    private List<String> loadPlanIds() {
        List<Integer> slots = SlotManager.slots(nodeManger.allAlive(), config.getHost(), config.getPort());
        if (CollectionUtils.isEmpty(slots)) {
            return Collections.emptyList();
        }
        List<PlanSlotEntity> slotEntities = planSlotEntityRepo.findBySlotIn(slots);
        if (CollectionUtils.isEmpty(slotEntities)) {
            return Collections.emptyList();
        }

        return slotEntities.stream()
                .map(PlanSlotEntity::getPlanId)
                .collect(Collectors.toList());
    }

}
