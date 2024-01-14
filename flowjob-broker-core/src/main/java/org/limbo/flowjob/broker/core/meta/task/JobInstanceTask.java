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
import org.limbo.flowjob.api.constants.JobStatus;
import org.limbo.flowjob.broker.core.agent.AgentRegistry;
import org.limbo.flowjob.broker.core.agent.ScheduleAgent;
import org.limbo.flowjob.broker.core.meta.job.JobInstance;
import org.limbo.flowjob.broker.core.meta.processor.JobDispatchSelect;
import org.limbo.flowjob.broker.core.schedule.scheduler.meta.MetaTask;
import org.limbo.flowjob.common.thread.CommonThreadPool;
import org.limbo.flowjob.common.utils.time.TimeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2024/1/5
 */
@Slf4j
public class JobInstanceTask extends MetaTask {

    private final JobInstance jobInstance;

    private final AgentRegistry agentRegistry;

    public JobInstanceTask(JobInstance jobInstance, AgentRegistry agentRegistry) {
        this.jobInstance = jobInstance;
        this.agentRegistry = agentRegistry;
    }

    @Override
    public void execute() {
        CommonThreadPool.IO.submit(() -> dispatch(jobInstance));
    }

    /**
     * 下发job给agent
     */
    public void dispatch(JobInstance jobInstance) {
        if (jobInstance.getStatus() != JobStatus.SCHEDULING) {
            return;
        }

        // 选择 agent
        List<ScheduleAgent> agents = agentRegistry.all().stream()
                .filter(a -> a.getAvailableQueueLimit() > 0)
                .filter(ScheduleAgent::isEnabled)
                .collect(Collectors.toList());
        ScheduleAgent agent = JobDispatchSelect.select(agents);
        if (agent == null) {
            // 状态检测的时候自动重试
            if (log.isDebugEnabled()) {
                log.debug("No alive server for job={}", jobInstance.getId());
            }
            return;
        }

        // rpc 执行
        try {
            log.info("Try dispatch JobInstance id={} to agent={}", jobInstance.getId(), agent.getId());
            boolean dispatched = agent.dispatch(jobInstance); // 可能存在接口超时导致重复下发，HttpBrokerApi.API_JOB_EXECUTING 由对应接口处理
            log.info("Dispatch JobInstance id={} to agent={} success={}", jobInstance.getId(), agent.getId(), dispatched);
        } catch (Exception e) {
            log.error("Dispatch JobInstance id={} to agent={} fail", jobInstance.getId(), agent.getId(), e);
        }
    }

    @Override
    public LocalDateTime scheduleAt() {
        return jobInstance.getTriggerAt();
    }

    @Override
    public String getType() {
        return "Job_Instance";
    }

    @Override
    public String getMetaId() {
        return jobInstance.getId();
    }

    @Override
    public boolean verify() {
        long delay = Duration.between(TimeUtils.currentLocalDateTime(), scheduleAt()).toMillis();
        delay = delay < 0 ? 0 : delay;

        return delay <= JobScheduleCheckTask.INTERVAL;
    }
}
