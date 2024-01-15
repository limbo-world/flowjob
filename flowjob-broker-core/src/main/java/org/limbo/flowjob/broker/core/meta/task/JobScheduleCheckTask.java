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

package org.limbo.flowjob.broker.core.meta.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.agent.AgentRegistry;
import org.limbo.flowjob.broker.core.cluster.Broker;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.meta.job.JobInstance;
import org.limbo.flowjob.broker.core.meta.job.JobInstanceRepository;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTaskScheduler;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 处理长时间还在调度中，未进行下发的 JobInstance
 */
@Slf4j
public class JobScheduleCheckTask {

    private final MetaTaskScheduler scheduler;

    private final Broker broker;

    private final NodeManger nodeManger;

    private final AgentRegistry agentRegistry;

    private final JobInstanceRepository jobInstanceRepository;

    public static final long INTERVAL = 10000; // 10s

    public JobScheduleCheckTask(MetaTaskScheduler scheduler,
                                Broker broker,
                                NodeManger nodeManger,
                                AgentRegistry agentRegistry,
                                JobInstanceRepository jobInstanceRepository) {
        this.scheduler = scheduler;
        this.broker = broker;
        this.nodeManger = nodeManger;
        this.agentRegistry = agentRegistry;
        this.jobInstanceRepository = jobInstanceRepository;
    }

    public void init() {
        new Timer().schedule(new InnerTask(), 0, Duration.ofMillis(INTERVAL).toMillis());
    }

    private class InnerTask extends TimerTask {

        @Override
        public void run() {
            try {
                // 判断自己是否存在 --- 可能由于心跳异常导致不存活
                if (!nodeManger.alive(broker.getRpcBaseURL().toString())) {
                    return;
                }

                // 一段时候后还是 还是 SCHEDULING 状态的，需要重新调度
                Integer limit = 100;
                String startId = "";
                LocalDateTime currentTime = TimeUtils.currentLocalDateTime();

                List<JobInstance> jobInstances = jobInstanceRepository.findInSchedule(broker.getRpcBaseURL(), currentTime.plus(-INTERVAL, ChronoUnit.MILLIS), currentTime, startId, limit);
                while (CollectionUtils.isNotEmpty(jobInstances)) {
                    for (JobInstance jobInstance : jobInstances) {
                        JobInstanceTask metaTask = new JobInstanceTask(jobInstance, agentRegistry);
                        scheduler.schedule(metaTask);
                    }
                    startId = jobInstances.get(jobInstances.size() - 1).getId();
                    jobInstances = jobInstanceRepository.findInSchedule(broker.getRpcBaseURL(), currentTime.plus(-INTERVAL, ChronoUnit.MILLIS), currentTime, startId, limit);
                }
            } catch (Exception e) {
                log.error("[{}] execute fail", this.getClass().getSimpleName(), e);
            }
        }
    }

}
