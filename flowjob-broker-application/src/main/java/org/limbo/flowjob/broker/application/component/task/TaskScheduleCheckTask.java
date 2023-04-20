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

package org.limbo.flowjob.broker.application.component.task;

import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.application.component.SlotManager;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixDelayMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;
import org.limbo.flowjob.broker.core.schedule.strategy.ITaskScheduleStrategy;
import org.limbo.flowjob.broker.dao.converter.DomainConverter;
import org.limbo.flowjob.broker.dao.entity.TaskEntity;
import org.limbo.flowjob.broker.dao.repositories.TaskEntityRepo;
import org.limbo.flowjob.common.constants.TaskStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

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
@Component
public class TaskScheduleCheckTask extends FixDelayMetaTask {

    private final TaskEntityRepo taskEntityRepo;

    private final SlotManager slotManager;

    private final Broker broker;

    private final NodeManger nodeManger;

    private final ITaskScheduleStrategy iScheduleStrategy;

    public TaskScheduleCheckTask(MetaTaskScheduler metaTaskScheduler,
                                 TaskEntityRepo taskEntityRepo,
                                 SlotManager slotManager,
                                 @Lazy Broker broker,
                                 NodeManger nodeManger,
                                 ITaskScheduleStrategy iScheduleStrategy) {
        super(Duration.ofSeconds(1), metaTaskScheduler);
        this.taskEntityRepo = taskEntityRepo;
        this.slotManager = slotManager;
        this.broker = broker;
        this.nodeManger = nodeManger;
        this.iScheduleStrategy = iScheduleStrategy;
    }

    @Override
    protected void executeTask() {
        // 判断自己是否存在 --- 可能由于心跳异常导致不存活
        if (!nodeManger.alive(broker.getName())) {
            return;
        }

        List<Task> dispatchingTasks = loadDispatchingTasks();
        if (CollectionUtils.isEmpty(dispatchingTasks)) {
            return;
        }
        for (Task task : dispatchingTasks) {
            iScheduleStrategy.schedule(task);
        }
    }

    /**
     * 加载下发中的 task。
     */
    private List<Task> loadDispatchingTasks() {
        List<String> planIds = slotManager.planIds();
        if (CollectionUtils.isEmpty(planIds)) {
            return Collections.emptyList();
        }
        List<TaskEntity> taskEntities = taskEntityRepo.findByPlanIdInAndStatus(planIds, TaskStatus.SCHEDULING.status);
        if (CollectionUtils.isEmpty(taskEntities)) {
            return Collections.emptyList();
        }
        return taskEntities.stream().map(DomainConverter::toTask).collect(Collectors.toList());
    }

    @Override
    public MetaTaskType getType() {
        return MetaTaskType.TASK_DISPATCH_CHECK;
    }

    @Override
    public String getMetaId() {
        return "TaskDispatchCheckTask";
    }

}
