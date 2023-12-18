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

package org.limbo.flowjob.broker.core.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.broker.application.component.BrokerSlotManager;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.job.JobInstance;
import org.limbo.flowjob.broker.core.domain.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.schedule.SchedulerProcessor;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.FixDelayMetaTask;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.broker.dao.entity.JobInstanceEntity;
import org.limbo.flowjob.broker.dao.repositories.JobInstanceEntityRepo;
import org.limbo.flowjob.common.utils.time.TimeUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 处理长时间还在调度中，未进行下发的 JobInstance
 */
@Slf4j
@Component
// todo
public class JobScheduleCheckTask extends FixDelayMetaTask {

    private final Broker broker;

    private final NodeManger nodeManger;

    private final JobInstanceEntityRepo jobInstanceEntityRepo;

    private final BrokerSlotManager slotManager;

    private final SchedulerProcessor schedulerProcessor;

    private final JobInstanceRepository jobInstanceRepository;

    private static final long INTERVAL = 10;

    public JobScheduleCheckTask(MetaTaskScheduler scheduler,
                                @Lazy Broker broker,
                                NodeManger nodeManger,
                                SchedulerProcessor schedulerProcessor,
                                JobInstanceRepository jobInstanceRepository,
                                JobInstanceEntityRepo jobInstanceEntityRepo,
                                BrokerSlotManager slotManager) {
        super(Duration.ofSeconds(INTERVAL), scheduler);
        this.broker = broker;
        this.nodeManger = nodeManger;
        this.schedulerProcessor = schedulerProcessor;
        this.jobInstanceRepository = jobInstanceRepository;
        this.jobInstanceEntityRepo = jobInstanceEntityRepo;
        this.slotManager = slotManager;
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
            String startId = "";
            LocalDateTime currentTime = TimeUtils.currentLocalDateTime();

            List<JobInstanceEntity> jobInstanceEntities = jobInstanceEntityRepo.findInSchedule(planIds, currentTime.plusSeconds(-INTERVAL), currentTime, JobStatus.SCHEDULING.status, startId, limit);
            while (CollectionUtils.isNotEmpty(jobInstanceEntities)) {
                for (JobInstanceEntity entity : jobInstanceEntities) {
                    JobInstance jobInstance = jobInstanceRepository.get(entity.getJobInstanceId());
                    try {
                        schedulerProcessor.schedule(jobInstance);
                    } catch (Exception e) {
                        log.error("jobInstance {} schedule fail", jobInstance.getJobInstanceId(), e);
                    }
                }
                startId = jobInstanceEntities.get(jobInstanceEntities.size() - 1).getJobInstanceId();
                jobInstanceEntities = jobInstanceEntityRepo.findInSchedule(planIds, currentTime.plusSeconds(-INTERVAL), currentTime, JobStatus.SCHEDULING.status, startId, limit);
            }
        } catch (Exception e) {
            log.error("{} execute fail", scheduleId(), e);
        }
    }

    @Override
    public String getType() {
        return "JOB_SCHEDULE_CHECK";
    }

    @Override
    public String getMetaId() {
        return this.getClass().getSimpleName();
    }

}
