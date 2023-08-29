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
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.broker.application.component.BrokerSlotManager;
import org.limbo.flowjob.broker.application.converter.MetaTaskConverter;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixDelayMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskType;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 处理长时间还在调度中，未进行下发的 PlanInstance
 */
@Slf4j
@Component
public class JobScheduleCheckTask extends FixDelayMetaTask {

    private final Broker broker;

    private final NodeManger nodeManger;

    private final JobInstanceEntityRepo jobInstanceEntityRepo;

    private final BrokerSlotManager slotManager;

    private final MetaTaskConverter metaTaskConverter;

    private static final long INTERVAL = 30;

    public JobScheduleCheckTask(MetaTaskScheduler scheduler,
                                @Lazy Broker broker,
                                NodeManger nodeManger,
                                JobInstanceEntityRepo jobInstanceEntityRepo,
                                BrokerSlotManager slotManager,
                                MetaTaskConverter metaTaskConverter) {
        super(Duration.ofSeconds(INTERVAL), scheduler);
        this.broker = broker;
        this.nodeManger = nodeManger;
        this.jobInstanceEntityRepo = jobInstanceEntityRepo;
        this.slotManager = slotManager;
        this.metaTaskConverter = metaTaskConverter;
    }

    @Override
    protected void executeTask() {
        try {
            // 判断自己是否存在 --- 可能由于心跳异常导致不存活
            if (!nodeManger.alive(broker.getName())) {
                return;
            }

            List<String> planIds = slotManager.planIds();
            if (CollectionUtils.isEmpty(planIds)) {
                return;
            }

            // 一段时候后还是 还是 SCHEDULING 状态的，需要重新调度
            Integer limit = 100;
            LocalDateTime currentTime = TimeUtils.currentLocalDateTime();

            List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findInSchedule(planIds, currentTime.plusSeconds(-INTERVAL), currentTime, JobStatus.SCHEDULING.status, limit);
            while (CollectionUtils.isNotEmpty(jobInstanceEntities)) {
                for (JobInstanceEntity entity : jobInstanceEntities) {
                    JobScheduleTask jobScheduleTask = metaTaskConverter.toJobInstanceScheduleTask(entity);
                    jobScheduleTask.scheduleJob();
                }
            }
        } catch (Exception e) {
            log.error("{} execute fail", scheduleId(), e);
        }
    }

    @Override
    public MetaTaskType getType() {
        return MetaTaskType.JOB_SCHEDULE_CHECK;
    }

    @Override
    public String getMetaId() {
        return this.getClass().getSimpleName();
    }

}
