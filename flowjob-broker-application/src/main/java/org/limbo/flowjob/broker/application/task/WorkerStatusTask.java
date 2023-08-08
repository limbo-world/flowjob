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

package org.limbo.flowjob.broker.application.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.WorkerStatus;
import org.limbo.flowjob.broker.application.component.SlotManager;
import org.limbo.flowjob.broker.application.service.WorkerService;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixDelayMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;
import org.limbo.flowjob.broker.dao.entity.WorkerEntity;
import org.limbo.flowjob.broker.dao.entity.WorkerMetricEntity;
import org.limbo.flowjob.broker.dao.repositories.WorkerEntityRepo;
import org.limbo.flowjob.broker.dao.repositories.WorkerMetricEntityRepo;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 获取plan下发
 * 相比update的任务比较久
 * 此任务主要为防止 plan 调度中异常导致 在时间轮中丢失
 */
@Slf4j
@Component
public class WorkerStatusTask extends FixDelayMetaTask {

    private final WorkerEntityRepo workerEntityRepo;

    private final WorkerService workerService;

    private final WorkerMetricEntityRepo workerMetricEntityRepo;

    private final SlotManager slotManager;

    private final Broker broker;

    private final BrokerConfig brokerConfig;

    private final NodeManger nodeManger;

    public WorkerStatusTask(MetaTaskScheduler scheduler,
                            WorkerEntityRepo workerEntityRepo,
                            WorkerMetricEntityRepo workerMetricEntityRepo,
                            WorkerService workerService,
                            BrokerConfig brokerConfig,
                            SlotManager slotManager,
                            @Lazy Broker broker,
                            NodeManger nodeManger) {
        super(Duration.ofSeconds(30), scheduler);
        this.workerEntityRepo = workerEntityRepo;
        this.workerMetricEntityRepo = workerMetricEntityRepo;
        this.workerService = workerService;
        this.brokerConfig = brokerConfig;
        this.slotManager = slotManager;
        this.broker = broker;
        this.nodeManger = nodeManger;
    }

    /**
     * 执行元任务，从 DB 加载一批待调度的 Plan，放到调度器中去。
     */
    @Override
    protected void executeTask() {
        try {
            // 判断自己是否存在 --- 可能由于心跳异常导致不存活
            if (!nodeManger.alive(broker.getName())) {
                return;
            }

            // 调度当前时间以及未来的任务
            List<String> workerIds = slotManager.workerIds();
            if (CollectionUtils.isEmpty(workerIds)) {
                return;
            }
            List<WorkerEntity> workerEntities = workerEntityRepo.findByWorkerIdInAndDeleted(workerIds, false);
            if (CollectionUtils.isEmpty(workerEntities)) {
                return;
            }

            LocalDateTime now = TimeUtils.currentLocalDateTime();
            for (WorkerEntity workerEntity : workerEntities) {
                WorkerStatus currentStatus = WorkerStatus.parse(workerEntity.getStatus());
                WorkerMetricEntity workerMetricEntity = workerMetricEntityRepo.findById(workerEntity.getWorkerId()).get();
                if (WorkerStatus.RUNNING == currentStatus) {

                    if (workerMetricEntity.getLastHeartbeatAt().plus(brokerConfig.getWorker().getHeartbeatTimeout(), ChronoUnit.MILLIS).isBefore(now)) {
                        workerService.updateStatus(workerEntity.getWorkerId(), WorkerStatus.RUNNING.status, WorkerStatus.FUSING.status);
                    }

                } else if (WorkerStatus.FUSING == currentStatus) {
                    if (workerMetricEntity.getLastHeartbeatAt().plus(brokerConfig.getWorker().getHeartbeatTimeout() * 2, ChronoUnit.MILLIS).isBefore(now)) {
                        workerService.updateStatus(workerEntity.getWorkerId(), WorkerStatus.FUSING.status, WorkerStatus.TERMINATED.status);
                    }
                }
            }
        } catch (Exception e) {
            log.error("{} load and schedule plan task fail", scheduleId(), e);
        }
    }


    @Override
    public MetaTaskType getType() {
        return MetaTaskType.PLAN_LOAD;
    }

    @Override
    public String getMetaId() {
        return this.getClass().getSimpleName();
    }

}
